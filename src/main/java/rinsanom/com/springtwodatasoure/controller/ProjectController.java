package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.config.KeycloakUtils;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.security.TokenUserService;
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
    private final TokenUserService tokenUserService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Projects project) {
        try {
            // Extract user UUID from JWT token using centralized service
            String userUuid = tokenUserService.getCurrentUserUuid();

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
            // Validate that the current user can access this user's projects
            tokenUserService.validateUserAccess(userUuid);

            List<Projects> projects = projectService.findByUserUuid(userUuid);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to find projects for user",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/my-projects")
    public ResponseEntity<Object> getMyProjects() {
        try {
            // Get current user's UUID from token
            String userUuid = tokenUserService.getCurrentUserUuid();

            List<Projects> projects = projectService.findByUserUuid(userUuid);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to find your projects",
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
