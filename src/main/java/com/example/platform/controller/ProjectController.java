package com.example.platform.controller;

import com.example.platform.model.dto.ProjectRequest;
import com.example.platform.model.dto.ProjectResponse;
import com.example.platform.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;

    @PostMapping
    public ProjectResponse create(@Validated @RequestBody ProjectRequest request) {
        return service.createProject(request);
    }
}
