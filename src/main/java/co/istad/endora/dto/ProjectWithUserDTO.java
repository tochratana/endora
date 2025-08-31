package co.istad.endora.dto;

import co.istad.endora.entity.Projects;
import co.istad.endora.entity.User;

public record ProjectWithUserDTO(
        Projects projects,
        User user
) {
}
