package co.istad.endora.dto;

import lombok.Builder;

@Builder
public record ForgotPasswordOtpResponse(
        boolean verified,
        String message,
        String resetToken, // This token will be used for password reset
        long tokenExpiresIn // Token expiration time in seconds
) {
}
