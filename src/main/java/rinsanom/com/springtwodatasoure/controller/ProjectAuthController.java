package rinsanom.com.springtwodatasoure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.LoginRequest;
import rinsanom.com.springtwodatasoure.dto.LoginResponse;
import rinsanom.com.springtwodatasoure.dto.RegisterRequest;
import rinsanom.com.springtwodatasoure.dto.RegisterResponse;
import rinsanom.com.springtwodatasoure.security.TokenUserService;
import rinsanom.com.springtwodatasoure.service.AuthService;

import java.util.Map;

/**
 * Project-scoped authentication endpoints that become available when a
 * project defines a user table.  These endpoints delegate to the existing
 * {@link AuthService} implementation but expose the project identifier in
 * the URL to allow per-project routing and documentation.
 */
@RestController
@RequestMapping("/api/{projectId}/auth")
@RequiredArgsConstructor
public class ProjectAuthController {

    private final AuthService authService;
    private final TokenUserService tokenUserService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@PathVariable String projectId,
                                                     @Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@PathVariable String projectId,
                                               @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentUser(@PathVariable String projectId) {
        String userUuid = tokenUserService.getCurrentUserUuid();
        return ResponseEntity.ok(Map.of("userUuid", userUuid));
    }
}
