package co.istad.endora.dto;

import lombok.Builder;

@Builder
public record OtpResponse(
        String message,
        String otpId,
        long expiresIn // in seconds
) {}