package co.istad.endora.dto;

import lombok.Builder;

@Builder
public record OtpVerificationResponse(
        boolean verified,
        String message,
        String accessToken,
        String refreshToken,
        String userId
) {}