package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(Jwt jwt);
    UserProfileResponse updateUserProfile(Jwt jwt, UserProfileRequest request);
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    User createOrGetUser(String keycloakUserId, String displayName);
    List<User> getAllUsers();
    void deleteUser(String keycloakUserId);
}
