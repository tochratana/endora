package co.istad.endora.dto.user;

import lombok.Builder;

// User profile view - limited information for regular users
@Builder
public record UserProfileResponse(
        Long id,                    // From local DB
        String username,            // From Keycloak
        String email,               // From Keycloak
        Boolean emailVerified,      // From Keycloak
        String firstName,           // From Keycloak
        String lastName,            // From Keycloak
        String displayName,         // From local DB
        String profileImage,        // From local DB
        String preferences          // From local DB
) {}