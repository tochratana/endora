package rinsanom.com.springtwodatasoure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ApiDiscoveryController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Endora API Engine Backend");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("documentation", "/swagger-ui/index.html");
        response.put("health", "/actuator/health");

        Map<String, String> mainEndpoints = new HashMap<>();
        mainEndpoints.put("Authentication", "/api/auth");
        mainEndpoints.put("User Profile", "/api/profile");
        mainEndpoints.put("Admin", "/api/admin");
        mainEndpoints.put("API Documentation", "/swagger-ui/index.html");

        response.put("endpoints", mainEndpoints);
        return ResponseEntity.ok(response);
    }
}
