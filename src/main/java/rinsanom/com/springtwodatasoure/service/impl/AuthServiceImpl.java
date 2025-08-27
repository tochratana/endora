package rinsanom.com.springtwodatasoure.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import rinsanom.com.springtwodatasoure.dto.*;
import rinsanom.com.springtwodatasoure.entity.OtpEntity;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.OtpRepository;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.AuthService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${keycloak.server-url:http://localhost:9090}")
    private String serverUrl;

    @Value("${keycloak.realm:endora_api}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxOtpAttempts;

    private static final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Attempting to register user: {}", registerRequest.username());

        // Validate password confirmation
        if (!registerRequest.password().equals(registerRequest.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");
        }

        // Create user representation
        UserRepresentation user = getUserRepresentation(registerRequest);

        String keycloakUserId = null;

        try {
            // Create user in Keycloak
            try (Response response = keycloak.realm(realm).users().create(user)) {
                log.info("Keycloak response status: {}", response.getStatus());

                if (response.getStatus() == 201) { // HTTP 201 Created
                    // Extract user ID from Location header
                    String location = response.getHeaderString("Location");
                    if (location != null) {
                        keycloakUserId = location.substring(location.lastIndexOf("/") + 1);
                        log.info("User created successfully in Keycloak with ID: {}", keycloakUserId);

                        // Create user in local database
                        User localUser = User.builder()
                                .keycloakUserId(keycloakUserId)
                                .displayName(registerRequest.firstName() + " " + registerRequest.lastName())
                                .build();

                        User savedUser = userRepository.save(localUser);
                        log.info("User created successfully in local database with ID: {}", savedUser.getId());

                        // Send verification email
                        try {
                            keycloak.realm(realm)
                                    .users()
                                    .get(keycloakUserId)
                                    .sendVerifyEmail();
                            log.info("Verification email sent to: {}", registerRequest.email());
                        } catch (Exception e) {
                            log.warn("Failed to send verification email: {}", e.getMessage());
                            // Don't fail the registration if email sending fails
                        }

                        return RegisterResponse.builder()
                                .id(keycloakUserId)
                                .username(registerRequest.username())
                                .email(registerRequest.email())
                                .firstName(registerRequest.firstName())
                                .lastName(registerRequest.lastName())
                                .message("User registered successfully. Please check your email for verification.")
                                .build();
                    } else {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "User created but failed to get user ID");
                    }
                } else {
                    // Handle different error statuses
                    String errorMessage = "Failed to create user";
                    if (response.hasEntity()) {
                        try {
                            errorMessage = response.readEntity(String.class);
                        } catch (Exception e) {
                            log.error("Failed to read error response", e);
                        }
                    }

                    log.error("Failed to create user. Status: {}, Error: {}", response.getStatus(), errorMessage);

                    if (response.getStatus() == 409) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "User already exists with this username or email");
                    } else {
                        throw new ResponseStatusException(HttpStatus.valueOf(response.getStatus()),
                                errorMessage);
                    }
                }
            }
        } catch (ResponseStatusException e) {
            // If local user was created but Keycloak failed, clean up
            if (keycloakUserId != null) {
                try {
                    userRepository.findByKeycloakUserId(keycloakUserId)
                            .ifPresent(userRepository::delete);
                } catch (Exception cleanupEx) {
                    log.error("Failed to cleanup local user after Keycloak failure", cleanupEx);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to register user: " + e.getMessage());
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            log.info("Attempting to login user: {}", loginRequest.usernameOrEmail());

            // First, validate credentials with Keycloak
            Keycloak userKeycloak = Keycloak.getInstance(
                    serverUrl,
                    realm,
                    loginRequest.usernameOrEmail(),
                    loginRequest.password(),
                    clientId,
                    clientSecret
            );

            // Get user info from Keycloak to check email verification
            userKeycloak.tokenManager().getAccessToken(); // Validate credentials
            UserRepresentation userInfo = getUserInfo(loginRequest.usernameOrEmail());

            // Check if email is verified
            if (!userInfo.isEmailVerified()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Email not verified. Please verify your email before logging in.");
            }

            // If email is verified, send OTP instead of returning tokens
            OtpRequest otpRequest = new OtpRequest(userInfo.getEmail(), "LOGIN");
            sendOtpInternal(otpRequest);

            return LoginResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .tokenType("Bearer")
                    .expiresIn(0)
                    .userId(userInfo.getId())
                    .username(userInfo.getUsername())
                    .email(userInfo.getEmail())
                    .emailVerified(userInfo.isEmailVerified())
                    .message("OTP sent to your email. Please verify OTP to complete login.")
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.usernameOrEmail(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Override
    @Transactional
    public OtpResponse sendOtp(OtpRequest otpRequest) {
        try {
            log.info("Sending OTP to email: {}", otpRequest.email());

            // Clean up expired OTPs
            otpRepository.deleteExpiredOtps(LocalDateTime.now());

            // Invalidate any existing unused OTPs for this email and purpose
            otpRepository.markUsedByEmailAndPurpose(otpRequest.email(), otpRequest.purpose());

            // Generate 6-digit OTP
            String otpCode = String.format("%06d", random.nextInt(999999));

            // Save OTP to database
            OtpEntity otpEntity = OtpEntity.builder()
                    .email(otpRequest.email())
                    .otpCode(otpCode)
                    .purpose(otpRequest.purpose() != null ? otpRequest.purpose() : "LOGIN")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .used(false)
                    .attempts(0)
                    .build();

            OtpEntity savedOtp = otpRepository.save(otpEntity);

            // Send OTP email
            sendOtpEmail(otpRequest.email(), otpCode);

            return OtpResponse.builder()
                    .message("OTP sent successfully")
                    .otpId(savedOtp.getId().toString())
                    .expiresIn(otpExpiryMinutes * 60L) // Convert to seconds
                    .build();

        } catch (Exception e) {
            log.error("Failed to send OTP to email: {}", otpRequest.email(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest verificationRequest) {
        try {
            log.info("Verifying OTP for email: {}", verificationRequest.email());

            // Find the OTP
            Optional<OtpEntity> otpOptional = otpRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                    verificationRequest.email(), "LOGIN");

            if (otpOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid OTP found");
            }

            OtpEntity otp = otpOptional.get();

            // Check if OTP is expired
            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
            }

            // Check attempts
            if (otp.getAttempts() >= maxOtpAttempts) {
                otp.setUsed(true);
                otpRepository.save(otp);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum OTP attempts exceeded");
            }

            // Increment attempts
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);

            // Verify OTP
            if (!otp.getOtpCode().equals(verificationRequest.otp())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
            }

            // Mark OTP as used
            otp.setUsed(true);
            otpRepository.save(otp);

            // Generate tokens for the user after successful OTP verification
            TokenResponse tokens = generateTokensForUser(verificationRequest.email());

            // Get user info for response
            UserRepresentation userInfo = getUserInfoByEmail(verificationRequest.email());

            return OtpVerificationResponse.builder()
                    .verified(true)
                    .message("OTP verified successfully")
                    .accessToken(tokens.accessToken())
                    .refreshToken(tokens.refreshToken())
                    .userId(userInfo.getId())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify OTP for email: {}", verificationRequest.email(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to verify OTP: " + e.getMessage());
        }
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest refreshRequest) {
        try {
            log.info("Refreshing token");

            // Use RestTemplate to call Keycloak's token endpoint directly
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "refresh_token");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("refresh_token", refreshRequest.refreshToken());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl, request, AccessTokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AccessTokenResponse tokenResponse = response.getBody();

                return TokenResponse.builder()
                        .accessToken(tokenResponse.getToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .tokenType("Bearer")
                        .expiresIn(tokenResponse.getExpiresIn())
                        .build();
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to refresh token");
            }

        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    @Override
    public void verify(String userId) {
        try {
            log.info("Sending verification email for user ID: {}", userId);
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.sendVerifyEmail();
            log.info("Verification email sent successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send verification email for user ID: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email: " + e.getMessage());
        }
    }

    private UserRepresentation getUserRepresentation(RegisterRequest registerRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        user.setEmailVerified(false);
        user.setEnabled(true);

        // Set password credentials
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerRequest.password());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return user;
    }

    private UserRepresentation getUserInfo(String usernameOrEmail) {
        try {
            // Search for user by username or email
            List<UserRepresentation> usersByUsername = keycloak.realm(realm).users().search(usernameOrEmail, 0, 1);
            if (!usersByUsername.isEmpty()) {
                return usersByUsername.get(0);
            }

            // If not found by username, try by email
            List<UserRepresentation> usersByEmail = keycloak.realm(realm).users().search(null, null, null, usernameOrEmail, 0, 1);
            if (!usersByEmail.isEmpty()) {
                return usersByEmail.get(0);
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } catch (Exception e) {
            log.error("Failed to get user info for: {}", usernameOrEmail, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get user info");
        }
    }

    private void sendOtpInternal(OtpRequest otpRequest) {
        try {
            log.info("Sending OTP to email: {}", otpRequest.email());

            // Clean up expired OTPs
            otpRepository.deleteExpiredOtps(LocalDateTime.now());

            // Invalidate any existing unused OTPs for this email and purpose
            otpRepository.markUsedByEmailAndPurpose(otpRequest.email(), otpRequest.purpose());

            // Generate 6-digit OTP
            String otpCode = String.format("%06d", random.nextInt(999999));

            // Save OTP to database
            OtpEntity otpEntity = OtpEntity.builder()
                    .email(otpRequest.email())
                    .otpCode(otpCode)
                    .purpose(otpRequest.purpose() != null ? otpRequest.purpose() : "LOGIN")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .used(false)
                    .attempts(0)
                    .build();

            OtpEntity savedOtp = otpRepository.save(otpEntity);

            // Send OTP email
            sendOtpEmail(otpRequest.email(), otpCode);

            log.info("OTP sent successfully with ID: {}", savedOtp.getId());

        } catch (Exception e) {
            log.error("Failed to send OTP to email: {}", otpRequest.email(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send OTP: " + e.getMessage());
        }
    }

    private TokenResponse generateTokensForUser(String email) {
        try {
            // Get user by email to find their username
            UserRepresentation user = getUserInfoByEmail(email);

            // We need to use a different approach since we don't have the user's password
            // We'll use the admin client to impersonate the user and generate tokens
            // This requires proper Keycloak configuration for service account impersonation

            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // First get service account token
            MultiValueMap<String, String> serviceAccountBody = new LinkedMultiValueMap<>();
            serviceAccountBody.add("grant_type", "client_credentials");
            serviceAccountBody.add("client_id", clientId);
            serviceAccountBody.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> serviceAccountRequest = new HttpEntity<>(serviceAccountBody, headers);

            ResponseEntity<AccessTokenResponse> serviceAccountResponse = restTemplate.postForEntity(
                    tokenUrl, serviceAccountRequest, AccessTokenResponse.class);

            if (!serviceAccountResponse.getStatusCode().is2xxSuccessful() || serviceAccountResponse.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get service account token");
            }

            // Now use token exchange to get user token (if supported by your Keycloak configuration)
            // Alternatively, we can create a session for the user programmatically

            // For now, let's create a simple JWT-like token structure that your application can validate
            // In a production environment, you should use proper Keycloak token exchange or
            // implement a custom token provider

            String accessToken = generateJwtToken(user, 3600); // 1 hour
            String refreshToken = generateJwtToken(user, 86400); // 24 hours (refresh token)

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate tokens for user: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate tokens: " + e.getMessage());
        }
    }

    private String generateJwtToken(UserRepresentation user, long expirationSeconds) {
        // This is a simplified token generation - in production you should use proper JWT libraries
        // and sign the tokens with your application's secret key

        long currentTime = System.currentTimeMillis() / 1000;
        long expirationTime = currentTime + expirationSeconds;

        // Create a simple token structure (you should use a proper JWT library like jjwt)
        String tokenPayload = String.format(
            "{\"sub\":\"%s\",\"email\":\"%s\",\"username\":\"%s\",\"iat\":%d,\"exp\":%d,\"iss\":\"endora_api\"}",
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            currentTime,
            expirationTime
        );

        // In production, encode and sign this payload properly
        // For now, we'll use base64 encoding (NOT SECURE - use proper JWT signing)
        return java.util.Base64.getEncoder().encodeToString(tokenPayload.getBytes());
    }

    private UserRepresentation getUserInfoByEmail(String email) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().search(null, null, null, email, 0, 1);
            if (users.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            return users.get(0);
        } catch (Exception e) {
            log.error("Failed to get user info by email: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get user info by email");
        }
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in " + otpExpiryMinutes + " minutes.");

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
        }
    }
}
