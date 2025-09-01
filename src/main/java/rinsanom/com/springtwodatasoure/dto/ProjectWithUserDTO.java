package rinsanom.com.springtwodatasoure.dto;

import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.entity.User;

public record ProjectWithUserDTO(
        Projects projects,
        User user
) {
}
