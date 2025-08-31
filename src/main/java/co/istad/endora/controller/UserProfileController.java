package co.istad.endora.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import co.istad.endora.dto.user.PublicUserProfileResponse;
import co.istad.endora.dto.user.UserProfileRequest;
import co.istad.endora.dto.user.UserProfileResponse;
import co.istad.endora.service.UserProfileService;


@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Get current user's profile
     * GET /api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        log.info("User requesting their profile");
        UserProfileResponse profile = userProfileService.getCurrentUserProfile(authentication);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     * PUT /api/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileRequest request) {
        log.info("User updating their profile");
        UserProfileResponse updatedProfile = userProfileService.updateCurrentUserProfile(authentication, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update profile image only
     * PATCH /api/profile/me/image
     */
    @PatchMapping("/me/image")
    public ResponseEntity<UserProfileResponse> updateProfileImage(
            Authentication authentication,
            @RequestParam String imageUrl) {
        log.info("User updating their profile image");
        UserProfileResponse updatedProfile = userProfileService.updateProfileImage(authentication, imageUrl);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update preferences only
     * PATCH /api/profile/me/preferences
     */
    @PatchMapping("/me/preferences")
    public ResponseEntity<UserProfileResponse> updatePreferences(
            Authentication authentication,
            @RequestBody String preferences) {
        log.info("User updating their preferences");
        UserProfileResponse updatedProfile = userProfileService.updatePreferences(authentication, preferences);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Delete current user's account
     * DELETE /api/profile/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUserAccount(Authentication authentication) {
        log.info("User requesting account deletion");
        userProfileService.deleteCurrentUserAccount(authentication);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get public profile of another user by username
     * GET /api/profile/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<PublicUserProfileResponse> getPublicUserProfile(@PathVariable String username) {
        log.info("Requesting public profile for username: {}", username);
        PublicUserProfileResponse profile = userProfileService.getPublicUserProfile(username);
        return ResponseEntity.ok(profile);
    }
}