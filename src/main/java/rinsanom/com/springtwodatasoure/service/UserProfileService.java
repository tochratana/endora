package rinsanom.com.springtwodatasoure.service;

import org.springframework.security.core.Authentication;
import rinsanom.com.springtwodatasoure.dto.user.UserProfileRequest;
import rinsanom.com.springtwodatasoure.dto.user.UserProfileResponse;
import rinsanom.com.springtwodatasoure.dto.user.PublicUserProfileResponse;

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