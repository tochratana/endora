package co.istad.endora.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import co.istad.endora.entity.User;
import co.istad.endora.exception.UnauthorizedException;
import co.istad.endora.repository.postgrest.UserRepository;

@Service
public class TokenUserService {

    private final UserRepository userRepository;

    public TokenUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extracts user UUID from the current authentication token
     * @return The user UUID from the token
     * @throws UnauthorizedException if no valid token or user UUID is found
     */
    public String getCurrentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return extractUserUuidFromToken(authentication);
    }

    /**
     * Extracts user UUID from the provided authentication token
     * @param authentication The authentication object containing the JWT token
     * @return The user UUID from the token
     * @throws UnauthorizedException if no valid token or user UUID is found
     */
    public String extractUserUuidFromToken(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No valid authentication token found");
        }

        // Get Keycloak user ID from JWT token
        String keycloakUserId = jwt.getClaimAsString("sub"); // Standard subject claim
        if (keycloakUserId == null || keycloakUserId.trim().isEmpty()) {
            throw new UnauthorizedException("Unable to extract Keycloak user ID from authentication token");
        }

        // Find the user in PostgreSQL database by Keycloak ID and get internal UUID
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UnauthorizedException("User not found with Keycloak ID: " + keycloakUserId +
                    ". Please ensure the user is registered in the system."));

        return user.getUuid();
    }

    /**
     * Gets the current JWT token
     * @return The current JWT token
     * @throws UnauthorizedException if no valid token is found
     */
    public Jwt getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No valid JWT token found");
        }
        return jwt;
    }

    /**
     * Gets the current Keycloak user ID from token
     * @return The Keycloak user ID
     * @throws UnauthorizedException if no valid token is found
     */
    public String getCurrentKeycloakUserId() {
        Jwt jwt = getCurrentJwtToken();
        String keycloakUserId = jwt.getClaimAsString("sub");
        if (keycloakUserId == null || keycloakUserId.trim().isEmpty()) {
            throw new UnauthorizedException("Unable to extract Keycloak user ID from token");
        }
        return keycloakUserId;
    }

    /**
     * Checks if the current user has a specific role
     * @param role The role to check for
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Checks if the current user is an admin
     * @return true if the user has admin role, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Validates that the current user UUID matches the provided UUID
     * Used for authorization checks
     * @param userUuid The UUID to validate against
     * @throws UnauthorizedException if the UUIDs don't match and user is not admin
     */
    public void validateUserAccess(String userUuid) {
        String currentUserUuid = getCurrentUserUuid();
        if (!currentUserUuid.equals(userUuid) && !isAdmin()) {
            throw new UnauthorizedException("Access denied: You can only access your own resources");
        }
    }

    /**
     * Creates or gets a user based on the current JWT token
     * This is useful for auto-creating users on first access
     * @return The user entity
     */
    public User getCurrentUser() {
        Jwt jwt = getCurrentJwtToken();
        String keycloakUserId = getCurrentKeycloakUserId();
        String username = jwt.getClaimAsString("preferred_username");

        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    // Auto-create user if doesn't exist
                    User newUser = User.builder()
                            .keycloakUserId(keycloakUserId)
                            .displayName(username)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
