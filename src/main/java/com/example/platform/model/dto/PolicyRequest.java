package com.example.platform.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyRequest(
        @NotBlank String table,
        @NotBlank String action,
        @NotBlank String role,
        String condition
) {}
