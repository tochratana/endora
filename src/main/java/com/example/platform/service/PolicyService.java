package com.example.platform.service;

import com.example.platform.model.dto.PolicyRequest;
import com.example.platform.util.SchemaName;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final JdbcTemplate jdbc;
    private final Cache<PolicyKey, Set<String>> cache = Caffeine.newBuilder().build();

    public boolean can(UUID projectId, String table, String action, Collection<String> roles) {
        Set<String> allowed = cache.get(new PolicyKey(projectId, table, action), this::loadRoles);
        return roles.stream().anyMatch(allowed::contains);
    }

    private Set<String> loadRoles(PolicyKey key) {
        String schema = SchemaName.from(key.projectId());
        String sql = "select role from " + schema + ".policies where table_name=? and action=?";
        return new HashSet<>(jdbc.query(sql, (rs, row) -> rs.getString("role"), key.table(), key.action()));
    }

    public void upsertPolicy(UUID projectId, PolicyRequest req) {
        String schema = SchemaName.from(projectId);
        String sql = "insert into " + schema + ".policies(table_name, action, role, condition) values (?,?,?,?) " +
                "on conflict (table_name, action, role) do update set condition=excluded.condition";
        jdbc.update(sql, req.table(), req.action(), req.role(), req.condition());
        cache.invalidate(new PolicyKey(projectId, req.table(), req.action()));
    }

    private record PolicyKey(UUID projectId, String table, String action) {}
}
