package rinsanom.com.springtwodatasoure.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.security.TokenUserService;
import rinsanom.com.springtwodatasoure.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenUserService tokenUserService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        // Get current user's JWT token through the centralized service
        UserProfileResponse profile = userService.getCurrentUserProfile(tokenUserService.getCurrentJwtToken());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileRequest request) {
        // Get current user's JWT token through the centralized service
        UserProfileResponse updatedProfile = userService.updateUserProfile(tokenUserService.getCurrentJwtToken(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        // This endpoint requires ADMIN role (already secured in SecurityConfig)
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{keycloakUserId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakUserId) {
        // This endpoint requires ADMIN role (should be secured in SecurityConfig)
        userService.deleteUser(keycloakUserId);
        return ResponseEntity.noContent().build();
    }
}
