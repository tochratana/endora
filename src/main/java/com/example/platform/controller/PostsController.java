package com.example.platform.controller;

import com.example.platform.model.dto.PostRequest;
import com.example.platform.util.ProjectSecurity;
import com.example.platform.util.SchemaName;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/{projectId}/posts")
@RequiredArgsConstructor
public class PostsController {
    private final JdbcTemplate jdbc;

    @GetMapping
    @PreAuthorize("@policyEvaluator.can(#projectId, 'posts', 'select', authentication)")
    public List<Map<String, Object>> all(@PathVariable UUID projectId, Authentication auth) {
        ProjectSecurity.verifyProject(projectId, auth);
        String schema = SchemaName.from(projectId);
        return jdbc.queryForList("select * from " + schema + ".posts");
    }

    @PostMapping
    @PreAuthorize("@policyEvaluator.can(#projectId, 'posts', 'insert', authentication)")
    public void create(@PathVariable UUID projectId, @RequestBody PostRequest req, Authentication auth) {
        ProjectSecurity.verifyProject(projectId, auth);
        String schema = SchemaName.from(projectId);
        jdbc.update("insert into " + schema + ".posts(title, content) values (?,?)", req.title(), req.content());
    }
}
