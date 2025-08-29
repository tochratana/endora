package rinsanom.com.springtwodatasoure.dto.user;

import lombok.Builder;

@Builder
public record PublicUserProfileResponse(
        String username,
        String displayName,
        String profileImage
) {}
