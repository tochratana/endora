package com.example.platform.service;

import com.example.platform.model.Project;
import com.example.platform.model.dto.ProjectRequest;
import com.example.platform.model.dto.ProjectResponse;
import com.example.platform.repository.ProjectRepository;
import com.example.platform.util.SchemaName;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository repository;
    private final JdbcTemplate jdbc;

    public ProjectResponse createProject(ProjectRequest request) {
        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setName(request.name());
        p.setAuthEnabled(request.authEnabled());
        p.setCreatedAt(Instant.now());
        repository.save(p);
        if (p.isAuthEnabled()) {
            provision(p.getId());
        }
        return new ProjectResponse(p.getId(), p.getName(), p.isAuthEnabled(), p.getCreatedAt());
    }

    private void provision(UUID projectId) {
        String schema = SchemaName.from(projectId);
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS citext");
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".users (" +
                "id UUID PRIMARY KEY DEFAULT gen_random_uuid()," +
                "username TEXT UNIQUE NOT NULL," +
                "email CITEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "created_at TIMESTAMPTZ DEFAULT now() NOT NULL," +
                "extra JSONB NOT NULL DEFAULT '{}'" +
                ")");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".roles (" +
                "id SERIAL PRIMARY KEY," +
                "name TEXT UNIQUE NOT NULL" +
                ")");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".user_roles (" +
                "user_id UUID REFERENCES " + schema + ".users(id) ON DELETE CASCADE," +
                "role TEXT NOT NULL," +
                "PRIMARY KEY(user_id, role)" +
                ")");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".policies (" +
                "id SERIAL PRIMARY KEY," +
                "table_name TEXT NOT NULL," +
                "action TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "condition TEXT NULL" +
                ")");
        jdbc.update("INSERT INTO " + schema + ".roles(name) VALUES ('authenticated') ON CONFLICT DO NOTHING");
    }
}
