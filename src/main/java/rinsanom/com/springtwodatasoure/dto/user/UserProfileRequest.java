package rinsanom.com.springtwodatasoure.dto.user;

import lombok.Builder;

@Builder
public record UserProfileRequest(
        String displayName,
        String profileImage,
        String preferences
) {}
