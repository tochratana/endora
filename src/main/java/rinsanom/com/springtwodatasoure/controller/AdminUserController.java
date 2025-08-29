package rinsanom.com.springtwodatasoure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.admin.AdminUserResponse;
import rinsanom.com.springtwodatasoure.dto.admin.AdminUserUpdateRequest;
import rinsanom.com.springtwodatasoure.dto.admin.UserSummaryResponse;
import rinsanom.com.springtwodatasoure.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Get all users for admin dashboard
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        log.info("Admin requested all users");
        List<AdminUserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get paginated users for admin dashboard
     * GET /api/admin/users/paginated
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsersPaginated(Pageable pageable) {
        log.info("Admin requested paginated users - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<AdminUserResponse> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get specific user by Keycloak ID
     * GET /api/admin/users/{keycloakUserId}
     */
    @GetMapping("/{keycloakUserId}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String keycloakUserId) {
        log.info("Admin requested user details: {}", keycloakUserId);
        AdminUserResponse user = adminUserService.getUserById(keycloakUserId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user by admin
     * PUT /api/admin/users/{keycloakUserId}
     */
    @PutMapping("/{keycloakUserId}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable String keycloakUserId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("Admin updating user: {}", keycloakUserId);
        AdminUserResponse updatedUser = adminUserService.updateUser(keycloakUserId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{keycloakUserId}
     */
    @DeleteMapping("/{keycloakUserId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakUserId) {
        log.info("Admin deleting user: {}", keycloakUserId);
        adminUserService.deleteUser(keycloakUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle user enabled/disabled status
     * PATCH /api/admin/users/{keycloakUserId}/status
     */
    @PatchMapping("/{keycloakUserId}/status")
    public ResponseEntity<AdminUserResponse> toggleUserStatus(
            @PathVariable String keycloakUserId,
            @RequestParam boolean enabled) {
        log.info("Admin {} user: {}", enabled ? "enabling" : "disabling", keycloakUserId);
        AdminUserResponse user = adminUserService.toggleUserStatus(keycloakUserId, enabled);
        return ResponseEntity.ok(user);
    }

    /**
     * Add role to user
     * POST /api/admin/users/{keycloakUserId}/roles/{roleName}
     */
    @PostMapping("/{keycloakUserId}/roles/{roleName}")
    public ResponseEntity<AdminUserResponse> addRoleToUser(
            @PathVariable String keycloakUserId,
            @PathVariable String roleName) {
        log.info("Admin adding role {} to user: {}", roleName, keycloakUserId);
        AdminUserResponse user = adminUserService.addRoleToUser(keycloakUserId, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * Remove role from user
     * DELETE /api/admin/users/{keycloakUserId}/roles/{roleName}
     */
    @DeleteMapping("/{keycloakUserId}/roles/{roleName}")
    public ResponseEntity<AdminUserResponse> removeRoleFromUser(
            @PathVariable String keycloakUserId,
            @PathVariable String roleName) {
        log.info("Admin removing role {} from user: {}", roleName, keycloakUserId);
        AdminUserResponse user = adminUserService.removeRoleFromUser(keycloakUserId, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user statistics for admin dashboard
     * GET /api/admin/users/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<UserSummaryResponse> getUserSummary() {
        log.info("Admin requested user summary");
        UserSummaryResponse summary = adminUserService.getUserSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Search users by criteria
     * GET /api/admin/users/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<List<AdminUserResponse>> searchUsers(@RequestParam String q) {
        log.info("Admin searching users with term: {}", q);
        List<AdminUserResponse> users = adminUserService.searchUsers(q);
        return ResponseEntity.ok(users);
    }
}