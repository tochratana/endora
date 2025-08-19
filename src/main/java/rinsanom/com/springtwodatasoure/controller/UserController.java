package rinsanom.com.springtwodatasoure.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.UserProfileResponse;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        UserProfileResponse profile = userService.getCurrentUserProfile(authentication);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse updatedProfile = userService.updateUserProfile(authentication, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{keycloakUserId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakUserId) {
        userService.deleteUser(keycloakUserId);
        return ResponseEntity.noContent().build();
    }
}

