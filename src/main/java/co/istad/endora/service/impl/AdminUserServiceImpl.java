package co.istad.endora.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.istad.endora.dto.admin.AdminUserResponse;
import co.istad.endora.dto.admin.AdminUserUpdateRequest;
import co.istad.endora.dto.admin.RoleSummary;
import co.istad.endora.dto.admin.UserSummaryResponse;
import co.istad.endora.entity.User;
import co.istad.endora.repository.postgrest.UserRepository;
import co.istad.endora.service.AdminUserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource:}")
    private String clientId;

    @Override
    public List<AdminUserResponse> getAllUsers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> keycloakUsers = usersResource.list();
            List<User> localUsers = userRepository.findAll();

            return keycloakUsers.stream()
                    .map(keycloakUser -> buildAdminUserResponse(keycloakUser, localUsers))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to get all users for admin", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve users");
        }
    }

    @Override
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        List<AdminUserResponse> allUsers = getAllUsers();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsers.size());

        List<AdminUserResponse> pageContent = allUsers.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allUsers.size());
    }

    @Override
    public AdminUserResponse getUserById(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserRepresentation keycloakUser = realmResource.users().get(keycloakUserId).toRepresentation();
            List<User> localUsers = userRepository.findAll();

            return buildAdminUserResponse(keycloakUser, localUsers);

        } catch (Exception e) {
            log.error("Failed to get user by ID: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(String keycloakUserId, AdminUserUpdateRequest request) {
        try {
            // Update local DB
            Optional<User> localUserOpt = userRepository.findByKeycloakUserId(keycloakUserId);
            if (localUserOpt.isPresent() && request.displayName() != null) {
                User localUser = localUserOpt.get();
                localUser.setDisplayName(request.displayName());
                if (request.status() != null) {
                    // Assuming you have a status field in User entity
                    // localUser.setStatus(request.status());
                }
                userRepository.save(localUser);
            }

            // Update Keycloak
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);
            UserRepresentation userRep = userResource.toRepresentation();

            if (request.enabled() != null) {
                userRep.setEnabled(request.enabled());
                userResource.update(userRep);
            }

            // Handle role updates
            if (request.rolesToAdd() != null && !request.rolesToAdd().isEmpty()) {
                for (String roleName : request.rolesToAdd()) {
                    addRoleToUser(keycloakUserId, roleName);
                }
            }

            if (request.rolesToRemove() != null && !request.rolesToRemove().isEmpty()) {
                for (String roleName : request.rolesToRemove()) {
                    removeRoleFromUser(keycloakUserId, roleName);
                }
            }

            return getUserById(keycloakUserId);

        } catch (Exception e) {
            log.error("Failed to update user: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update user");
        }
    }

    @Override
    @Transactional
    public void deleteUser(String keycloakUserId) {
        try {
            // Delete from local database first
            userRepository.findByKeycloakUserId(keycloakUserId)
                    .ifPresent(userRepository::delete);

            // Delete from Keycloak
            RealmResource realmResource = keycloak.realm(realm);
            Response response = realmResource.users().delete(keycloakUserId);

            if (response.getStatus() != 204 && response.getStatus() != 404) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to delete user from Keycloak");
            }

            log.info("Admin deleted user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Admin failed to delete user: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete user");
        }
    }

    @Override
    public AdminUserResponse toggleUserStatus(String keycloakUserId, boolean enabled) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);
            UserRepresentation userRep = userResource.toRepresentation();

            userRep.setEnabled(enabled);
            userResource.update(userRep);

            log.info("Admin {} user: {}", enabled ? "enabled" : "disabled", keycloakUserId);

            return getUserById(keycloakUserId);

        } catch (Exception e) {
            log.error("Failed to toggle user status: {}", keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update user status");
        }
    }

    @Override
    public AdminUserResponse addRoleToUser(String keycloakUserId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            // Try to add as realm role first
            try {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().add(Arrays.asList(role));
                log.info("Added realm role {} to user: {}", roleName, keycloakUserId);
            } catch (Exception e) {
                // If realm role doesn't exist, try client role
                if (clientId != null && !clientId.isEmpty()) {
                    String clientUuid = realmResource.clients().findByClientId(clientId).get(0).getId();
                    RoleRepresentation clientRole = realmResource.clients().get(clientUuid).roles().get(roleName).toRepresentation();
                    userResource.roles().clientLevel(clientUuid).add(Arrays.asList(clientRole));
                    log.info("Added client role {} to user: {}", roleName, keycloakUserId);
                } else {
                    throw e;
                }
            }

            return getUserById(keycloakUserId);

        } catch (Exception e) {
            log.error("Failed to add role {} to user: {}", roleName, keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to add role to user");
        }
    }

    @Override
    public AdminUserResponse removeRoleFromUser(String keycloakUserId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            // Try to remove from realm roles first
            try {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().remove(Arrays.asList(role));
                log.info("Removed realm role {} from user: {}", roleName, keycloakUserId);
            } catch (Exception e) {
                // If not realm role, try client role
                if (clientId != null && !clientId.isEmpty()) {
                    String clientUuid = realmResource.clients().findByClientId(clientId).get(0).getId();
                    RoleRepresentation clientRole = realmResource.clients().get(clientUuid).roles().get(roleName).toRepresentation();
                    userResource.roles().clientLevel(clientUuid).remove(Arrays.asList(clientRole));
                    log.info("Removed client role {} from user: {}", roleName, keycloakUserId);
                } else {
                    throw e;
                }
            }

            return getUserById(keycloakUserId);

        } catch (Exception e) {
            log.error("Failed to remove role {} from user: {}", roleName, keycloakUserId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to remove role from user");
        }
    }

    @Override
    public UserSummaryResponse getUserSummary() {
        try {
            List<AdminUserResponse> allUsers = getAllUsers();

            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(user -> user.enabled()).count();
            long inactiveUsers = totalUsers - activeUsers;
            long verifiedEmailUsers = allUsers.stream().filter(user -> user.emailVerified()).count();
            long unverifiedEmailUsers = totalUsers - verifiedEmailUsers;

            // Role distribution
            Map<String, Long> roleCount = new HashMap<>();
            allUsers.forEach(user -> {
                if (user.roles() != null) {
                    user.roles().forEach(role ->
                            roleCount.merge(role, 1L, Long::sum)
                    );
                }
            });

            List<RoleSummary> roleDistribution = roleCount.entrySet().stream()
                    .map(entry -> RoleSummary.builder()
                            .roleName(entry.getKey())
                            .userCount(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

            return UserSummaryResponse.builder()
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .inactiveUsers(inactiveUsers)
                    .verifiedEmailUsers(verifiedEmailUsers)
                    .unverifiedEmailUsers(unverifiedEmailUsers)
                    .roleDistribution(roleDistribution)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get user summary", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to get user summary");
        }
    }

    @Override
    public List<AdminUserResponse> searchUsers(String searchTerm) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Search in Keycloak
            List<UserRepresentation> searchResults = usersResource.search(searchTerm);
            List<User> localUsers = userRepository.findAll();

            return searchResults.stream()
                    .map(keycloakUser -> buildAdminUserResponse(keycloakUser, localUsers))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to search users with term: {}", searchTerm, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to search users");
        }
    }

    private AdminUserResponse buildAdminUserResponse(UserRepresentation keycloakUser, List<User> localUsers) {
        // Find corresponding local user
        Optional<User> localUser = localUsers.stream()
                .filter(u -> u.getKeycloakUserId().equals(keycloakUser.getId()))
                .findFirst();

        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .keycloakUserId(keycloakUser.getId())
                .username(keycloakUser.getUsername())
                .email(keycloakUser.getEmail())
                .emailVerified(keycloakUser.isEmailVerified())
                .firstName(keycloakUser.getFirstName())
                .lastName(keycloakUser.getLastName())
                .enabled(keycloakUser.isEnabled())
                .roles(getUserAllRoles(keycloakUser.getId()))
                .realmRoles(getUserRealmRoles(keycloakUser.getId()))
                .clientRoles(getUserClientRoles(keycloakUser.getId()));

        // Add creation timestamp
        if (keycloakUser.getCreatedTimestamp() != null) {
            builder.createdTimestamp(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(keycloakUser.getCreatedTimestamp()),
                            ZoneId.systemDefault())
            );
        }

        // Add local DB data if exists
        if (localUser.isPresent()) {
            User user = localUser.get();
            builder.id(user.getId())
                    .displayName(user.getDisplayName())
                    .profileImage(user.getProfileImage())
                    .preferences(user.getPreferences());
            // .status(user.getStatus()) // if you have status field
            // .lastLogin(user.getLastLogin()) // if you track last login
        }

        return builder.build();
    }

    // Helper methods for roles (similar to previous implementation)
    private List<String> getUserAllRoles(String keycloakUserId) {
        List<String> allRoles = new ArrayList<>();
        allRoles.addAll(getUserRealmRoles(keycloakUserId));
        allRoles.addAll(getUserClientRoles(keycloakUserId));
        return allRoles;
    }

    private List<String> getUserRealmRoles(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            List<RoleRepresentation> realmRoles = userResource.roles().realmLevel().listEffective();
            return realmRoles.stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get realm roles for user: {}", keycloakUserId, e);
            return new ArrayList<>();
        }
    }

    private List<String> getUserClientRoles(String keycloakUserId) {
        try {
            if (clientId == null || clientId.trim().isEmpty()) {
                return new ArrayList<>();
            }

            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            String clientUuid = realmResource.clients().findByClientId(clientId).get(0).getId();
            List<RoleRepresentation> clientRoles = userResource.roles().clientLevel(clientUuid).listEffective();

            return clientRoles.stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get client roles for user: {}", keycloakUserId, e);
            return new ArrayList<>();
        }
    }
}