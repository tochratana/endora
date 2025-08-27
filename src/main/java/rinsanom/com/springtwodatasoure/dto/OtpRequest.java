package rinsanom.com.springtwodatasoure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        String purpose // LOGIN, PASSWORD_RESET, etc.
) {}