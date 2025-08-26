package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.service.DynamicOpenApiService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/openapi")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OpenApiController {

    private final DynamicOpenApiService dynamicOpenApiService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getOpenApiSpec(@PathVariable String projectId) {
        try {
            Map<String, Object> openApiSpec = dynamicOpenApiService.generateOpenApiForProject(projectId);
            return ResponseEntity.ok(openApiSpec);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate OpenAPI spec: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}