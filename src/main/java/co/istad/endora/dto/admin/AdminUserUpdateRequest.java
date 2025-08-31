package co.istad.endora.dto.admin;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminUserUpdateRequest(
        String displayName,
        Boolean enabled,
        List<String> rolesToAdd,
        List<String> rolesToRemove,
        String status
) {}
