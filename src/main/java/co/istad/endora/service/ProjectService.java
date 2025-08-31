package co.istad.endora.service;

import co.istad.endora.dto.ProjectWithUserDTO;
import co.istad.endora.entity.Projects;

import java.util.List;

public interface ProjectService {
    Projects save(Projects project);
    List<Projects> findAll();
    Projects findById(String id);
    void deleteById(String id);
    ProjectWithUserDTO findProjectWithUser(String projectId);
    Projects findByProjectUuid(String projectUuid); // Added method to find by project UUID
    List<Projects> findByUserUuid(String userUuid); // Added method to find projects by user UUID
}
