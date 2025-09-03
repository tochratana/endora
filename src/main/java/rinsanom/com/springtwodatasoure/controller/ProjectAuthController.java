package rinsanom.com.springtwodatasoure.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.entity.TableData;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.repository.mongo.TableDataRepository;
import rinsanom.com.springtwodatasoure.service.JwtService;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client-api/{projectId}/auth")
@RequiredArgsConstructor
@Slf4j
public class ProjectAuthController {

    private final TableService tableService;
    private final TableDataRepository tableDataRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@PathVariable String projectId,
                                      @RequestBody Map<String, Object> request) {
        try {
            // Check if users table exists
            TableSchema usersTable = tableService.getTableByNameAndProject("users", projectId);
            if (usersTable == null) {
                usersTable = tableService.getTableByNameAndProject("user", projectId);
            }

            if (usersTable == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Authentication not enabled for this project"
                ));
            }

            String email = (String) request.get("email");
            String password = (String) request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Email and password are required"
                ));
            }

            // Check if user already exists
            List<TableData> existingUsers = tableDataRepository
                    .findBySchemaNameAndProjectId(usersTable.getSchemaName(), projectId);

            boolean userExists = existingUsers.stream()
                    .anyMatch(user -> email.equals(user.getData().get("email")));

            if (userExists) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "User with this email already exists"
                ));
            }

            // Hash password and create user
            String hashedPassword = passwordEncoder.encode(password);
            Map<String, Object> userData = new HashMap<>(request);
            userData.put("email", email);
            userData.put("password_hash", hashedPassword);
            userData.remove("password"); // Remove plain password

            tableService.insertData(usersTable.getSchemaName(), projectId, userData);

            // Find the created user to get the ID
            List<TableData> users = tableDataRepository
                    .findBySchemaNameAndProjectId(usersTable.getSchemaName(), projectId);

            TableData createdUser = users.stream()
                    .filter(user -> email.equals(user.getData().get("email")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Failed to create user"));

            // Generate JWT
            String token = jwtService.generateToken(createdUser.getId(), projectId, email);

            Map<String, Object> userResponse = new HashMap<>(createdUser.getData());
            userResponse.put("id", createdUser.getId());
            userResponse.remove("password_hash"); // Don't return password hash

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", userResponse
            ));

        } catch (Exception e) {
            log.error("Registration failed for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@PathVariable String projectId,
                                   @RequestBody Map<String, Object> request) {
        try {
            // Check if users table exists
            TableSchema usersTable = tableService.getTableByNameAndProject("users", projectId);
            if (usersTable == null) {
                usersTable = tableService.getTableByNameAndProject("user", projectId);
            }

            if (usersTable == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Authentication not enabled for this project"
                ));
            }

            String email = (String) request.get("email");
            String password = (String) request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Email and password are required"
                ));
            }

            // Find user by email
            List<TableData> users = tableDataRepository
                    .findBySchemaNameAndProjectId(usersTable.getSchemaName(), projectId);

            TableData user = users.stream()
                    .filter(u -> email.equals(u.getData().get("email")))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid credentials"
                ));
            }

            // Verify password
            String storedHash = (String) user.getData().get("password_hash");
            if (!passwordEncoder.matches(password, storedHash)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid credentials"
                ));
            }

            // Generate JWT
            String token = jwtService.generateToken(user.getId(), projectId, email);

            Map<String, Object> userResponse = new HashMap<>(user.getData());
            userResponse.put("id", user.getId());
            userResponse.remove("password_hash"); // Don't return password hash

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", userResponse
            ));

        } catch (Exception e) {
            log.error("Login failed for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Login failed"
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@PathVariable String projectId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid authorization header"
                ));
            }

            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token, projectId);

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);

            // Find user by ID
            TableSchema usersTable = tableService.getTableByNameAndProject("users", projectId);
            if (usersTable == null) {
                usersTable = tableService.getTableByNameAndProject("user", projectId);
            }

            if (usersTable == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Authentication not enabled for this project"
                ));
            }

            Map<String, Object> userRecord = tableService.getRecordById(usersTable.getSchemaName(), userId);
            if (userRecord == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "User not found"
                ));
            }

            userRecord.remove("password_hash"); // Don't return password hash
            return ResponseEntity.ok(userRecord);

        } catch (Exception e) {
            log.error("Get current user failed for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or expired token"
            ));
        }
    }
}