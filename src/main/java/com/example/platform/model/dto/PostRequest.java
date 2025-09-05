package com.example.platform.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PostRequest(@NotBlank String title, @NotBlank String content) {}
