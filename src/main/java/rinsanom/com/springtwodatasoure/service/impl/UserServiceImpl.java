package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rinsanom.com.springtwodatasoure.config.KeycloakUtils;
import rinsanom.com.springtwodatasoure.dto.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakUtils keycloakUtils;

    @Override
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);
        String username = keycloakUtils.getUsername(authentication);
        String email = keycloakUtils.getEmail(authentication);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    // Create user if doesn't exist (for users created before this system)
                    log.info("Creating missing user record for Keycloak user: {}", keycloakUserId);
                    return createOrGetUser(keycloakUserId, username);
                });

        return UserProfileResponse.builder()
                .id(user.getId())
                .keycloakUserId(user.getKeycloakUserId())
                .username(username)
                .email(email)
                .displayName(user.getDisplayName())
                .profileImage(user.getProfileImage())
                .preferences(user.getPreferences())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Authentication authentication, UserProfileRequest request) {
        String keycloakUserId = keycloakUtils.getKeycloakUserId(authentication);

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update user fields
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
        log.info("Updated user profile for Keycloak user: {}", keycloakUserId);

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .keycloakUserId(updatedUser.getKeycloakUserId())
                .username(keycloakUtils.getUsername(authentication))
                .email(keycloakUtils.getEmail(authentication))
                .displayName(updatedUser.getDisplayName())
                .profileImage(updatedUser.getProfileImage())
                .preferences(updatedUser.getPreferences())
                .build();
    }

    @Override
    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        return userRepository.findByKeycloakUserId(keycloakUserId);
    }

    @Override
    @Transactional
    public User createOrGetUser(String keycloakUserId, String displayName) {
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakUserId(keycloakUserId)
                            .displayName(displayName)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(String keycloakUserId) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
        log.info("Deleted user with Keycloak ID: {}", keycloakUserId);
    }
}
