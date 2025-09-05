package com.example.platform.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(@NotBlank String name, boolean authEnabled) {}
