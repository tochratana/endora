package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    void verify(String userId);
    TokenResponse refreshToken(RefreshTokenRequest refreshRequest);

    // Improved Forgot Password functionality
    OtpResponse forgotPassword(String email);
    ForgotPasswordOtpResponse verifyForgotPasswordOtp(OtpVerificationRequest verificationRequest);
    String resetPasswordWithToken(String resetToken, String newPassword);

}