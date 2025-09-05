package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.mongo.ProjectRepository;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.ProjectService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;  // Mongo
    private final UserRepository userRepository;

    @Override
    public Projects save(Projects project) {
        // Validate that user exists by UUID
        userRepository.findByUuid(project.getUserUuid())
                .orElseThrow(() -> new RuntimeException("User with UUID " + project.getUserUuid() + " not found"));

        // Generate project UUID if not already set
        if (project.getProjectUuid() == null) {
            project.setProjectUuid(UUID.randomUUID().toString());
        }

        return projectRepository.save(project);
    }

    @Override
    public List<Projects> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Projects findById(String id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        projectRepository.deleteById(id);
    }

    public ProjectWithUserDTO findProjectWithUser(String projectId) {

        if (projectId == null || projectId.trim().isEmpty()) {
            throw new RuntimeException("Project ID cannot be null or empty");
        }

        // Find project
        Projects project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        // Validate userUuid
        if (project.getUserUuid() == null || project.getUserUuid().trim().isEmpty()) {
            throw new RuntimeException("Project does not have a valid user UUID");
        }

        // Find user by UUID
        User user = userRepository.findByUuid(project.getUserUuid())
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + project.getUserUuid()));

        return new ProjectWithUserDTO(project, user);
    }

    @Override
    public Projects findByProjectUuid(String projectUuid) {
        return projectRepository.findByProjectUuid(projectUuid).orElse(null);
    }

    @Override
    public List<Projects> findByUserUuid(String userUuid) {
        // Validate that user exists
        userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User with UUID " + userUuid + " not found"));

        return projectRepository.findByUserUuid(userUuid);
    }

    /**
     * Get user information for a specific project
     */
    public User getProjectUser(String projectId) {
        Projects project = findById(projectId);
        if (project == null) {
            throw new RuntimeException("Project not found with ID: " + projectId);
        }

        return userRepository.findByUuid(project.getUserUuid())
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + project.getUserUuid()));
    }

    /**
     * Setup authentication for a project
     * This method handles all authentication-related setup
     */
    private void setupAuthentication(Projects project) {
        try {
            // Generate JWT secret if not already set
            if (project.getJwtSecret() == null || project.getJwtSecret().trim().isEmpty()) {
                project.setJwtSecret(generateJwtSecret());
            }

            // Set default user table name if not provided
            if (project.getUserTableName() == null || project.getUserTableName().trim().isEmpty()) {
                project.setUserTableName("users");
            }

            // Create default user table
            createDefaultUserTable(project);

            // log.info("Authentication setup completed for project: {}", project.getProjectUuid());

        } catch (Exception e) {
            // log.error("Failed to setup authentication for project {}: {}", project.getProjectUuid(), e.getMessage());
            throw new RuntimeException("Failed to setup authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Creates default user table with predefined schema
     */
    private void createDefaultUserTable(Projects project) {
        // Create predefined user table schema
        Map<String, String> userTableSchema = createUserTableSchema();

        try {
            // Create the user table in the database
            // Uncomment when tableService is available
            // tableService.createTables(project.getProjectUuid(), project.getUserTableName(), userTableSchema);

            // Generate authentication endpoints
            // Uncomment when authScaffoldService is available
            // authScaffoldService.generateDefaultAuthEndpoints(project.getProjectUuid(), userTableSchema);

//            log.info("Created user table '{}' for project '{}' with authentication enabled",
//                    project.getUserTableName(), project.getProjectUuid());

        } catch (Exception e) {
            // log.error("Failed to create user table for project {}: {}", project.getProjectUuid(), e.getMessage());
            throw new RuntimeException("Failed to create user table: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the standard user table schema
     */
    private Map<String, String> createUserTableSchema() {
        Map<String, String> schema = new HashMap<>();
        schema.put("id", "UUID PRIMARY KEY DEFAULT gen_random_uuid()");
        schema.put("username", "VARCHAR(50) UNIQUE NOT NULL");
        schema.put("email", "VARCHAR(255) UNIQUE NOT NULL");
        schema.put("password_hash", "VARCHAR(255) NOT NULL");
        schema.put("user_role", "VARCHAR(20) DEFAULT 'USER'");
        schema.put("first_name", "VARCHAR(100)");
        schema.put("last_name", "VARCHAR(100)");
        schema.put("is_active", "BOOLEAN DEFAULT true");
        schema.put("email_verified", "BOOLEAN DEFAULT false");
        schema.put("phone_number", "VARCHAR(20)");
        schema.put("profile_image_url", "TEXT");
        schema.put("last_login_at", "TIMESTAMP");
        schema.put("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        schema.put("updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        return schema;
    }

    /**
     * Generates a secure JWT secret
     */
    private String generateJwtSecret() {
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Enable authentication for an existing project
     */
    public Projects enableAuthentication(String projectId) {
        Projects project = findById(projectId);
        if (project == null) {
            throw new RuntimeException("Project not found with ID: " + projectId);
        }

        if (project.isAuthenticationEnabled()) {
            // log.warn("Authentication is already enabled for project: {}", project.getProjectUuid());
            return project;
        }

        project.setAuthenticationEnabled(true);
        setupAuthentication(project);

        return projectRepository.save(project);
    }

    /**
     * Disable authentication for a project
     */
    public Projects disableAuthentication(String projectId) {
        Projects project = findById(projectId);
        if (project == null) {
            throw new RuntimeException("Project not found with ID: " + projectId);
        }

        project.setAuthenticationEnabled(false);
        project.setJwtSecret(null);

        // Note: This doesn't delete the user table, just disables authentication
        // You might want to add logic to handle user table cleanup if needed

        // log.info("Authentication disabled for project: {}", project.getProjectUuid());
        return projectRepository.save(project);
    }


}
