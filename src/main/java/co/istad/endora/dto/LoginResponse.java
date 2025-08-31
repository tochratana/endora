package co.istad.endora.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String userId,
        String username,
        String email,
        boolean emailVerified,
        String message
) {}