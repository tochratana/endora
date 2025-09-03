package rinsanom.com.springtwodatasoure.controller;

import rinsanom.com.springtwodatasoure.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.*;
import rinsanom.com.springtwodatasoure.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<String> verifyEmail(@PathVariable String userId) {
        try {
            authService.verify(userId);
            return ResponseEntity.ok("Verification email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification email: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            TokenResponse response = authService.refreshToken(refreshRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    // Forgot Password endpoints
    @PostMapping("/forgot-password")
    public ResponseEntity<OtpResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            OtpResponse response = authService.forgotPassword(forgotPasswordRequest.email());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    @PostMapping("/verify-forgot-password-otp")
    public ResponseEntity<ForgotPasswordOtpResponse> verifyForgotPasswordOtp(@Valid @RequestBody OtpVerificationRequest verificationRequest) {
        try {
            ForgotPasswordOtpResponse response = authService.verifyForgotPasswordOtp(verificationRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        try {
            String response = authService.resetPasswordWithToken(
                    newPasswordRequest.resetToken(),
                    newPasswordRequest.newPassword()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }
}