package rinsanom.com.springtwodatasoure.dto.admin;

import lombok.Builder;

@Builder
public record RoleSummary(
        String roleName,
        Long userCount
) {}
