package com.example.platform.controller;

import com.example.platform.model.dto.*;
import com.example.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/{projectId}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@PathVariable UUID projectId, @Validated @RequestBody RegisterRequest req) {
        service.register(projectId, req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public TokenResponse login(@PathVariable UUID projectId, @Validated @RequestBody LoginRequest req) {
        return service.login(projectId, req);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@PathVariable UUID projectId, @Validated @RequestBody RefreshRequest req) {
        // projectId from path is not used but kept for routing
        return service.refresh(req.refreshToken());
    }
}
