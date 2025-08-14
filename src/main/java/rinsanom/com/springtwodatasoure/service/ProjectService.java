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
}
