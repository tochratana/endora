package com.example.platform.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record RegisterRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password,
        Map<String, Object> extra
) {}
