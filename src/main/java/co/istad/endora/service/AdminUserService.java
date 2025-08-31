package co.istad.endora.service;

import co.istad.endora.dto.admin.AdminUserResponse;
import co.istad.endora.dto.admin.AdminUserUpdateRequest;
import co.istad.endora.dto.admin.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Admin User Service - Only accessible by users with ADMIN role
 * Provides complete user management functionality for administrators
 */
public interface AdminUserService {

    /**
     * Get all users with complete information for admin
     */
    List<AdminUserResponse> getAllUsers();

    /**
     * Get paginated users for admin dashboard
     */
    Page<AdminUserResponse> getAllUsers(Pageable pageable);

    /**
     * Get user by ID for admin
     */
    AdminUserResponse getUserById(String keycloakUserId);

    /**
     * Update user by admin
     */
    AdminUserResponse updateUser(String keycloakUserId, AdminUserUpdateRequest request);

    /**
     * Delete user (from both Keycloak and local DB)
     */
    void deleteUser(String keycloakUserId);

    /**
     * Enable/Disable user
     */
    AdminUserResponse toggleUserStatus(String keycloakUserId, boolean enabled);

    /**
     * Add role to user
     */
    AdminUserResponse addRoleToUser(String keycloakUserId, String roleName);

    /**
     * Remove role from user
     */
    AdminUserResponse removeRoleFromUser(String keycloakUserId, String roleName);

    /**
     * Get user statistics for admin dashboard
     */
    UserSummaryResponse getUserSummary();

    /**
     * Search users by criteria
     */
    List<AdminUserResponse> searchUsers(String searchTerm);
}