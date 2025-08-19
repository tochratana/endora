package rinsanom.com.springtwodatasoure.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rinsanom.com.springtwodatasoure.dto.RegisterRequest;
import rinsanom.com.springtwodatasoure.dto.RegisterResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.AuthService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @Value("${keycloak.realm:endora_api}")
    private String realm;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Attempting to register user: {}", registerRequest.username());

        // Validate password confirmation
        if (!registerRequest.password().equals(registerRequest.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");
        }

        // Create user representation
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

        String keycloakUserId = null;

        try {
            // Create user in Keycloak
            Response response = keycloak.realm(realm).users().create(user);

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
}
