package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.RegisterRequest;
import rinsanom.com.springtwodatasoure.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    void verify(String userId);
}
