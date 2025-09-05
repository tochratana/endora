package com.example.platform.controller;

import com.example.platform.model.dto.TableCreateRequest;
import com.example.platform.service.TableService;
import com.example.platform.util.ProjectSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/{projectId}/tables")
@RequiredArgsConstructor
public class TableController {
    private final TableService service;

    @PostMapping
    public ResponseEntity<Void> create(@PathVariable UUID projectId, @Validated @RequestBody TableCreateRequest req,
                                       Authentication auth) {
        ProjectSecurity.verifyProject(projectId, auth);
        service.createTable(projectId, req);
        return ResponseEntity.ok().build();
    }
}
