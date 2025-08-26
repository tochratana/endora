package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.service.ProjectService;

import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Projects project) {
        try {
            // Validate that userUuid is provided
            if (project.getUserUuid() == null || project.getUserUuid().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User UUID is required",
                    "message", "userUuid field must be provided to create a project"
                ));
            }

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
