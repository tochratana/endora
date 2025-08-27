package rinsanom.com.springtwodatasoure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
