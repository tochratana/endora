package com.example.platform.security;

import com.example.platform.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("policyEvaluator")
@RequiredArgsConstructor
public class PolicyEvaluator {
    private final PolicyService policyService;

    public boolean can(UUID projectId, String table, String action, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        return policyService.can(projectId, table, action, roles);
    }
}
