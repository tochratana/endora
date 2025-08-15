package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.entity.User;
import rinsanom.com.springtwodatasoure.repository.mongo.ProjectRepository;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.ProjectService;

import java.util.List;
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
}
