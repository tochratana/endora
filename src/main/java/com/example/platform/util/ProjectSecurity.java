package com.example.platform.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public final class ProjectSecurity {
    private ProjectSecurity() {}

    public static void verifyProject(UUID pathProjectId, Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();
            UUID claim = UUID.fromString(jwt.getClaimAsString("project_id"));
            if (!claim.equals(pathProjectId)) {
                throw new AccessDeniedException("Project mismatch");
            }
        } else {
            throw new AccessDeniedException("Invalid authentication");
        }
    }
}
