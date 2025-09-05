package com.example.platform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${jwt.access-token-validity}")
    private long accessValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshValidity;

    public String generateAccessToken(UUID userId, UUID projectId, List<String> roles) {
        return generateToken(userId, projectId, roles, accessValidity);
    }

    public String generateRefreshToken(UUID userId, UUID projectId, List<String> roles) {
        return generateToken(userId, projectId, roles, refreshValidity);
    }

    private String generateToken(UUID userId, UUID projectId, List<String> roles, long validitySeconds) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("project_id", projectId.toString())
                .claim("roles", roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(validitySeconds))
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
