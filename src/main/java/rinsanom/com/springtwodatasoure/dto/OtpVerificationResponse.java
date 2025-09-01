package rinsanom.com.springtwodatasoure.dto;

import lombok.Builder;

@Builder
public record OtpVerificationResponse(
        boolean verified,
        String message,
        String accessToken,
        String refreshToken,
        String userId
) {}