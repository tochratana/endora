package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.ProjectWithUserDTO;
import rinsanom.com.springtwodatasoure.entity.Projects;

import java.util.List;

public interface ProjectService {
    Projects save(Projects project);
    List<Projects> findAll();
    Projects findById(String id);
    void deleteById(String id);
    ProjectWithUserDTO findProjectWithUser(String projectId);
    Projects findByProjectUuid(String projectUuid); // Added method to find by project UUID
    List<Projects> findByUserUuid(String userUuid); // Added method to find projects by user UUID
    
    /**
     * Enable authentication for an existing project
     */
    Projects enableAuthentication(String projectId);

    /**
     * Disable authentication for a project
     */
    Projects disableAuthentication(String projectId);
}
