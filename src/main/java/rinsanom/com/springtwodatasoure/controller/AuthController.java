package rinsanom.com.springtwodatasoure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.RegisterRequest;
import rinsanom.com.springtwodatasoure.dto.RegisterResponse;
import rinsanom.com.springtwodatasoure.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw e; // Let global exception handler deal with it
        }
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<String> verifyEmail(@PathVariable String userId) {
        try {
            authService.verify(userId);
            return ResponseEntity.ok("Verification email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification email: " + e.getMessage());
        }
    }
}
