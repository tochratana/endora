package com.example.platform.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, boolean authEnabled, Instant createdAt) {}
