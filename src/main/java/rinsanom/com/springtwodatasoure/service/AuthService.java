package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    void verify(String userId);
    OtpResponse sendOtp(OtpRequest otpRequest);
    OtpVerificationResponse verifyOtp(OtpVerificationRequest verificationRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshRequest);
}