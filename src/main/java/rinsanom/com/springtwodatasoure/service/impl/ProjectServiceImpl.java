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

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;  // Mongo
    private final UserRepository userRepository;

    @Override
    public Projects save(Projects project) {
        userRepository.findById(Integer.valueOf(project.getUserId()))
                .orElseThrow(() -> new RuntimeException("User with ID " + project.getUserId() + " not found"));

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

        // Validate userId
        if (project.getUserId() == null || project.getUserId().trim().isEmpty()) {
            throw new RuntimeException("Project does not have a valid user ID");
        }

        // Parse and validate userId
        Integer userId;
        try {
            userId = Integer.valueOf(project.getUserId().trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID format: " + project.getUserId());
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return new ProjectWithUserDTO(project, user);
    }
}
