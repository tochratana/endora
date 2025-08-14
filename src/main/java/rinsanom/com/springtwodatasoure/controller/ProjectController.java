package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.service.ProjectService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Projects> createProject(@RequestBody Projects project) {
        return ResponseEntity.ok(projectService.save(project));
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
