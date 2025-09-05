package com.example.platform.service;

import com.example.platform.model.dto.TableCreateRequest;
import com.example.platform.util.SchemaName;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {
    private final JdbcTemplate jdbc;

    public void createTable(UUID projectId, TableCreateRequest request) {
        String schema = SchemaName.from(projectId);
        String cols = request.columns().stream()
                .map(c -> c.name() + " " + c.type())
                .collect(Collectors.joining(","));
        String sql = "CREATE TABLE " + schema + "." + request.name() + " (" + cols + ")";
        jdbc.execute(sql);
        jdbc.update("INSERT INTO project_tables(project_id, table_name) VALUES (?,?)", projectId, request.name());
    }
}
