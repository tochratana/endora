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
import rinsanom.com.springtwodatasoure.entity.PasswordResetTokenEntity;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.OtpRepository;
import rinsanom.com.springtwodatasoure.repository.postgrest.PasswordResetTokenRepository;
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

    private final Keycloak keycloak; // This is the admin client
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${keycloak.server-url:https://endora-oauth2.istad.co}")
    private String serverUrl;

    @Value("${keycloak.realm:endora_api}")
    private String realm;

    // FIXED: Separate admin and user client configurations
    @Value("${keycloak.client-id:spring-main}")
    private String userClientId;

    @Value("${keycloak.client-secret}")
    private String userClientSecret;

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
            // Create user in Keycloak using admin client
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

            // FIXED: Use direct token endpoint call instead of Keycloak.getInstance
            AccessTokenResponse tokenResponse = authenticateUser(
                    loginRequest.usernameOrEmail(),
                    loginRequest.password()
            );

            // Get user info using admin client
            UserRepresentation userInfo = getUserInfo(loginRequest.usernameOrEmail());

            // Check if email is verified
            if (!userInfo.isEmailVerified()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Email not verified. Please verify your email before logging in.");
            }

            // Return actual Keycloak tokens
            return LoginResponse.builder()
                    .accessToken(tokenResponse.getToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(tokenResponse.getExpiresIn())
                    .userId(userInfo.getId())
                    .username(userInfo.getUsername())
                    .email(userInfo.getEmail())
                    .emailVerified(userInfo.isEmailVerified())
                    .message("Login successful")
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.usernameOrEmail(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    // NEW: Direct authentication method using token endpoint
    private AccessTokenResponse authenticateUser(String usernameOrEmail, String password) {
        try {
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "password");
            requestBody.add("client_id", userClientId);
            requestBody.add("client_secret", userClientSecret);
            requestBody.add("username", usernameOrEmail);
            requestBody.add("password", password);
            requestBody.add("scope", "openid profile email");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl, request, AccessTokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("User authenticated successfully: {}", usernameOrEmail);
                return response.getBody();
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
            }

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", usernameOrEmail, e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest refreshRequest) {
        try {
            log.info("Refreshing token");

            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "refresh_token");
            requestBody.add("client_id", userClientId); // FIXED: Use user client
            requestBody.add("client_secret", userClientSecret); // FIXED: Use user client secret
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

    // Rest of your methods remain the same...
    @Override
    @Transactional
    public OtpResponse forgotPassword(String email) {
        try {
            log.info("Processing forgot password request for email: {}", email);

            // Check if user exists with this email
            UserRepresentation user = getUserInfoByEmail(email);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this email");
            }

            // Clean up expired OTPs
            otpRepository.deleteExpiredOtps(LocalDateTime.now());

            // Invalidate any existing unused OTPs for this email and purpose
            otpRepository.markUsedByEmailAndPurpose(email, "FORGOT_PASSWORD");

            // Generate 6-digit OTP
            String otpCode = String.format("%06d", random.nextInt(999999));

            // Save OTP to database with FORGOT_PASSWORD purpose
            OtpEntity otpEntity = OtpEntity.builder()
                    .email(email)
                    .otpCode(otpCode)
                    .purpose("FORGOT_PASSWORD")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .used(false)
                    .attempts(0)
                    .build();

            OtpEntity savedOtp = otpRepository.save(otpEntity);

            // Send OTP email for password reset
            sendPasswordResetOtpEmail(email, otpCode);

            return OtpResponse.builder()
                    .message("Password reset OTP sent successfully")
                    .otpId(savedOtp.getId().toString())
                    .expiresIn(otpExpiryMinutes * 60L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to process forgot password for email: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send password reset OTP: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ForgotPasswordOtpResponse verifyForgotPasswordOtp(OtpVerificationRequest verificationRequest) {
        try {
            log.info("Verifying forgot password OTP for email: {}", verificationRequest.email());

            // Find the OTP with FORGOT_PASSWORD purpose
            Optional<OtpEntity> otpOptional = otpRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                    verificationRequest.email(), "FORGOT_PASSWORD");

            if (otpOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid password reset OTP found");
            }

            OtpEntity otp = otpOptional.get();

            // Check if OTP is expired
            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset OTP has expired");
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset OTP");
            }

            // Mark OTP as used
            otp.setUsed(true);
            otpRepository.save(otp);

            // Get user info
            UserRepresentation userInfo = getUserInfoByEmail(verificationRequest.email());

            // Clean up expired reset tokens
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());

            // Invalidate any existing reset tokens for this email
            passwordResetTokenRepository.markUsedByEmail(verificationRequest.email());

            // Generate a secure reset token
            String resetToken = generateSecureToken();

            // Save the reset token with 15-minute expiry
            PasswordResetTokenEntity resetTokenEntity = PasswordResetTokenEntity.builder()
                    .resetToken(resetToken)
                    .email(verificationRequest.email())
                    .userId(userInfo.getId())
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes to reset password
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetTokenEntity);

            return ForgotPasswordOtpResponse.builder()
                    .verified(true)
                    .message("OTP verified successfully. Use the reset token to set your new password.")
                    .resetToken(resetToken)
                    .tokenExpiresIn(15 * 60L) // 15 minutes in seconds
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify forgot password OTP for email: {}", verificationRequest.email(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to verify password reset OTP: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String resetPasswordWithToken(String resetToken, String newPassword) {
        try {
            log.info("Resetting password with reset token");

            // Clean up expired tokens
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());

            // Find the reset token
            Optional<PasswordResetTokenEntity> tokenOptional = passwordResetTokenRepository
                    .findByResetTokenAndUsedFalseAndExpiresAtAfter(resetToken, LocalDateTime.now());

            if (tokenOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired reset token. Please request a new password reset.");
            }

            PasswordResetTokenEntity tokenEntity = tokenOptional.get();

            // Get user from Keycloak
            UserRepresentation user = getUserInfoByEmail(tokenEntity.getEmail());
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            // Reset password in Keycloak
            UserResource userResource = keycloak.realm(realm).users().get(user.getId());

            CredentialRepresentation newCredential = new CredentialRepresentation();
            newCredential.setType(CredentialRepresentation.PASSWORD);
            newCredential.setValue(newPassword);
            newCredential.setTemporary(false);

            userResource.resetPassword(newCredential);

            // Mark token as used
            tokenEntity.setUsed(true);
            passwordResetTokenRepository.save(tokenEntity);

            log.info("Password reset successfully for user: {}", tokenEntity.getEmail());
            return "Password reset successfully";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to reset password with token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to reset password: " + e.getMessage());
        }
    }

    // Helper methods remain the same...
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

    private void sendPasswordResetOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset OTP");
            message.setText("Your password reset OTP code is: " + otp +
                    "\n\nThis code will expire in " + otpExpiryMinutes + " minutes." +
                    "\n\nIf you did not request a password reset, please ignore this email.");

            mailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send password reset OTP email");
        }
    }

    private String generateSecureToken() {
        // Generate a secure random token for password reset
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}