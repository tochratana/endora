package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;

import java.util.List;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(Authentication authentication);
    UserProfileResponse updateUserProfile(Authentication authentication, UserProfileRequest request);
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    User createOrGetUser(String keycloakUserId, String displayName);
    List<User> getAllUsers();
    void deleteUser(String keycloakUserId);
}
