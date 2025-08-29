package rinsanom.com.springtwodatasoure.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rinsanom.com.springtwodatasoure.config.KeycloakUtils;
import rinsanom.com.springtwodatasoure.dto.user.PublicUserProfileResponse;
import rinsanom.com.springtwodatasoure.dto.user.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.user.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.UserProfileService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final KeycloakUtils keycloakUtils;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);
        String username = keycloakUtils.getUsername(authentication);
        String email = keycloakUtils.getEmail(authentication);

        // Get additional info from Keycloak
        UserRepresentation keycloakUser = getKeycloakUser(keycloakUserId);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    log.info("Creating missing user record for Keycloak user: {}", keycloakUserId);
                    return createOrGetUser(keycloakUserId, username);
                });

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(username)
                .email(email)
                .emailVerified(keycloakUser.isEmailVerified())
                .firstName(keycloakUser.getFirstName())
                .lastName(keycloakUser.getLastName())
                .displayName(user.getDisplayName())
                .profileImage(user.getProfileImage())
                .preferences(user.getPreferences())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(Authentication authentication, UserProfileRequest request) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update local user fields
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.profileImage() != null) {
            user.setProfileImage(request.profileImage());
        }
        if (request.preferences() != null) {
            user.setPreferences(request.preferences());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated their profile: {}", keycloakUserId);

        // Return updated profile
        return getCurrentUserProfile(authentication);
    }

    @Override
    public PublicUserProfileResponse getPublicUserProfile(String username) {
        try {
            // Find user in Keycloak by username
            RealmResource realmResource = keycloak.realm(realm);
            UserRepresentation keycloakUser = realmResource.users()
                    .search(username)
                    .stream()
                    .filter(u -> username.equals(u.getUsername()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Get local user data
            Optional<User> localUser = userRepository.findByKeycloakUserId(keycloakUser.getId());

            PublicUserProfileResponse.PublicUserProfileResponseBuilder builder =
                    PublicUserProfileResponse.builder()
                            .username(keycloakUser.getUsername());

            if (localUser.isPresent()) {
                User user = localUser.get();
                builder.displayName(user.getDisplayName())
                        .profileImage(user.getProfileImage());
            } else {
                // Use Keycloak data as fallback
                String displayName = buildDisplayName(keycloakUser.getFirstName(), keycloakUser.getLastName());
                builder.displayName(displayName);
            }

            return builder.build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get public user profile for username: {}", username, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve user profile");
        }
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfileImage(Authentication authentication, String profileImageUrl) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setProfileImage(profileImageUrl);
        userRepository.save(user);

        log.info("User updated profile image: {}", keycloakUserId);
        return getCurrentUserProfile(authentication);
    }

    @Override
    @Transactional
    public UserProfileResponse updatePreferences(Authentication authentication, String preferences) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPreferences(preferences);
        userRepository.save(user);

        log.info("User updated preferences: {}", keycloakUserId);
        return getCurrentUserProfile(authentication);
    }

    @Override
    @Transactional
    public void deleteCurrentUserAccount(Authentication authentication) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);

        try {
            // Delete from local database first
            userRepository.findByKeycloakUserId(keycloakUserId)
                    .ifPresent(user -> {
                        userRepository.delete(user);
                        log.info("User deleted their local account: {}", keycloakUserId);
                    });

            // Delete from Keycloak
            RealmResource realmResource = keycloak.realm(realm);
            Response response = realmResource.users().delete(keycloakUserId);

            if (response.getStatus() == 204) {
                log.info("User successfully deleted their Keycloak account: {}", keycloakUserId);
            } else if (response.getStatus() == 404) {
                log.warn("User not found in Keycloak during deletion: {}", keycloakUserId);
            } else {
                log.error("Failed to delete user from Keycloak. Status: {}", response.getStatus());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to completely delete account");
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting user account: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete account");
        }
    }

    // Helper methods
    private UserRepresentation getKeycloakUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().get(keycloakUserId).toRepresentation();
        } catch (Exception e) {
            log.error("Failed to get Keycloak user: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Keycloak");
        }
    }

    @Transactional
    protected User createOrGetUser(String keycloakUserId, String displayName) {
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakUserId(keycloakUserId)
                            .displayName(displayName)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private String buildDisplayName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return "Unknown User";
        }
    }
}