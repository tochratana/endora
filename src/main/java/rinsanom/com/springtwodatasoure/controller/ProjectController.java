package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.config.KeycloakUtils;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.ProjectService;

import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final KeycloakUtils keycloakUtils;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(
            @RequestBody Projects project,
            Authentication authentication) {
        try {
            // Extract user UUID from JWT token
            String userUuid = extractUserUuidFromToken(authentication);

            // Set the userUuid from the token (override any provided userUuid)
            project.setUserUuid(userUuid);

            Projects savedProject = projectService.save(project);
            return ResponseEntity.ok(Map.of(
                "message", "Project created successfully",
                "project", savedProject,
                "projectUuid", savedProject.getProjectUuid(),
                "userUuid", savedProject.getUserUuid()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create project",
                "message", e.getMessage()
            ));
        }
    }

    private String extractUserUuidFromToken(Authentication authentication) {
        try {
            // Check if authentication is null
            if (authentication == null) {
                throw new RuntimeException("Authentication is null");
            }

            // Check if principal is JWT
            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                throw new RuntimeException("Authentication principal is not a JWT token. Type: " +
                    authentication.getPrincipal().getClass().getSimpleName());
            }

            // Debug: Print all available claims
            System.out.println("Available JWT claims: " + jwt.getClaims().keySet());
            jwt.getClaims().forEach((key, value) ->
                System.out.println("Claim: " + key + " = " + value));

            // Try to get Keycloak user ID from JWT token
            String keycloakUserId = jwt.getClaimAsString("sub");

            // If sub claim is not available, try session-based lookup
            if (keycloakUserId == null || keycloakUserId.trim().isEmpty()) {
                // This appears to be a client credentials token, not a user token
                // For client credentials tokens, we cannot determine the user
                throw new RuntimeException("This appears to be a client credentials token (azp=admin-cli). " +
                    "Please use a user access token obtained from /api/auth/login instead. " +
                    "Available claims: " + jwt.getClaims().keySet());
            }

            System.out.println("Extracted Keycloak user ID: " + keycloakUserId);

            final String finalKeycloakUserId = keycloakUserId;

            // Find the user in PostgreSQL database by Keycloak ID
            User user = userRepository.findByKeycloakUserId(finalKeycloakUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + finalKeycloakUserId));

            return user.getUuid();
        } catch (Exception e) {
            System.err.println("Error extracting user UUID: " + e.getMessage());
            throw new RuntimeException("Failed to extract user UUID: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Projects>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Projects> getProjectById(@PathVariable String id) {
        Projects project = projectService.findById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }


    @GetMapping("/uuid/{projectUuid}")
    public ResponseEntity<Object> getProjectByUuid(@PathVariable String projectUuid) {
        try {
            Projects project = projectService.findByProjectUuid(projectUuid);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to find project",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<Object> getProjectsByUserUuid(@PathVariable String userUuid) {
        try {
            List<Projects> projects = projectService.findByUserUuid(userUuid);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to find projects for user",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/with-user")
    public ResponseEntity<ProjectWithUserDTO> getProjectWithUser(@PathVariable String id) {
        return ResponseEntity.ok(projectService.findProjectWithUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
