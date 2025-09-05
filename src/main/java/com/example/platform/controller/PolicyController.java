package com.example.platform.controller;

import com.example.platform.model.dto.PolicyRequest;
import com.example.platform.service.PolicyService;
import com.example.platform.util.ProjectSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/{projectId}/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService service;

    @PostMapping
    public ResponseEntity<Void> upsert(@PathVariable UUID projectId, @Validated @RequestBody PolicyRequest req,
                                       Authentication auth) {
        ProjectSecurity.verifyProject(projectId, auth);
        service.upsertPolicy(projectId, req);
        return ResponseEntity.ok().build();
    }
}
