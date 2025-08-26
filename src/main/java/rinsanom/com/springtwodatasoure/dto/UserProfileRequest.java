package rinsanom.com.springtwodatasoure.dto;

import jakarta.validation.constraints.Size;

public record UserProfileRequest(
        @Size(max = 100, message = "Display name must be less than 100 characters")
        String displayName,

        String profileImage,

        String preferences
) {}
