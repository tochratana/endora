package rinsanom.com.springtwodatasoure.dto;

import lombok.Builder;

@Builder
public record RegisterResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String message
) {}
