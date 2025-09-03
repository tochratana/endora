package rinsanom.com.springtwodatasoure.dto.admin;

import lombok.Builder;

import java.util.List;

@Builder
public record UserSummaryResponse(
        Long totalUsers,
        Long activeUsers,
        Long inactiveUsers,
        Long verifiedEmailUsers,
        Long unverifiedEmailUsers,
        List<RoleSummary> roleDistribution
) {}
