package rinsanom.com.springtwodatasoure.dto;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        Integer id,
        String keycloakUserId,
        String username,
        String email,
        String displayName,
        String profileImage,
        String preferences
) {}
