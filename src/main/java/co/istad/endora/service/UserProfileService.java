package co.istad.endora.service;

import org.springframework.security.core.Authentication;
import co.istad.endora.dto.user.UserProfileRequest;
import co.istad.endora.dto.user.UserProfileResponse;
import co.istad.endora.dto.user.PublicUserProfileResponse;

public interface UserProfileService {

    /**
     * Get current user's own profile
     */
    UserProfileResponse getCurrentUserProfile(Authentication authentication);

    /**
     * Update current user's profile
     */
    UserProfileResponse updateCurrentUserProfile(Authentication authentication, UserProfileRequest request);

    /**
     * Get public profile of another user (limited information)
     */
    PublicUserProfileResponse getPublicUserProfile(String username);

    /**
     * Update profile image
     */
    UserProfileResponse updateProfileImage(Authentication authentication, String profileImageUrl);

    /**
     * Update preferences
     */
    UserProfileResponse updatePreferences(Authentication authentication, String preferences);

    /**
     * Delete current user's account
     */
    void deleteCurrentUserAccount(Authentication authentication);
}