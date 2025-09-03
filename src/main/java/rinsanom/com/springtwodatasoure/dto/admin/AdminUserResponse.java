package rinsanom.com.springtwodatasoure.dto.admin;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

// Admin view - complete user information
@Builder
public record AdminUserResponse(
        Long id,                          // From local DB
        String keycloakUserId,            // Keycloak user ID
        String username,                  // From Keycloak
        String email,                     // From Keycloak
        Boolean emailVerified,            // From Keycloak
        String firstName,                 // From Keycloak
        String lastName,                  // From Keycloak
        Boolean enabled,                  // From Keycloak
        LocalDateTime createdTimestamp,   // From Keycloak
        List<String> roles,               // All roles from Keycloak
        List<String> realmRoles,          // Realm roles only
        List<String> clientRoles,         // Client roles only
        String displayName,               // From local DB
        String profileImage,              // From local DB
        String preferences,               // From local DB
        LocalDateTime lastLogin,          // From local DB (if tracked)
        String status                     // Custom status from local DB
) {}
