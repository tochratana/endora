package co.istad.endora.dto.user;

import lombok.Builder;

@Builder
public record UserProfileRequest(
        String displayName,
        String profileImage,
        String preferences
) {}
