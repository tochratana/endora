package rinsanom.com.springtwodatasoure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewPasswordRequest(
        @NotBlank(message = "Reset token is required")
        String resetToken,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {
}
