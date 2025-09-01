package rinsanom.com.springtwodatasoure.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.mongodb.uri", matchIfMissing = false)
public class DynamicEndPointController {

    private final DynamicEndpointService dynamicEndpointService;
    private final TableService tableService;

    /**
     * Get documentation for all dynamically generated endpoints
     */
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> getAllEndpointDocumentation() {
        try {
            Map<String, String> allDocs = dynamicEndpointService.getAllEndpointDocumentation();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("totalTables", allDocs.size());
            response.put("tables", allDocs.keySet());
            response.put("documentation", allDocs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve documentation",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get documentation for a specific table's endpoints
     */
    @GetMapping("/docs/{tableName}")
    public ResponseEntity<Map<String, Object>> getEndpointDocumentation(@PathVariable String tableName) {
        try {
            String docs = dynamicEndpointService.getEndpointDocumentation(tableName);
            if (docs.contains("No documentation found")) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Documentation not found",
                    "tableName", tableName
                ));
            }

            // Parse the documentation into structured JSON
            Map<String, Object> structuredDocs = parseDocumentationToJson(docs, tableName);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "tableName", tableName,
                "documentation", structuredDocs
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve documentation",
                "tableName", tableName,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Generate endpoints for an existing table
     */
    @PostMapping("/generate/{tableName}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> generateEndpointsForTable(
            @PathVariable String tableName,
            @PathVariable String projectId) {
        try {
            // Get the table schema first
            TableSchema tableSchema = tableService.getTableByNameAndProject(tableName, projectId);
            if (tableSchema == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Table not found",
                    "tableName", tableName,
                    "projectId", projectId
                ));
            }

            // Generate endpoints
            dynamicEndpointService.generateEndpointsForTable(tableSchema);

            return ResponseEntity.ok(Map.of(
                "message", "Endpoints generated successfully",
                "tableName", tableName,
                "projectId", projectId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate endpoints",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Remove endpoints documentation for a table
     */
    @DeleteMapping("/docs/{tableName}")
    public ResponseEntity<Map<String, Object>> removeEndpointsForTable(@PathVariable String tableName) {
        try {
            dynamicEndpointService.removeEndpointsForTable(tableName);
            return ResponseEntity.ok(Map.of(
                "message", "Endpoints documentation removed successfully",
                "tableName", tableName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to remove endpoints documentation",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Regenerate endpoints for all tables in a project
     */
    @PostMapping("/regenerate/project/{projectId}")
    public ResponseEntity<Map<String, Object>> regenerateEndpointsForProject(@PathVariable String projectId) {
        try {
            List<TableSchema> tables = tableService.getTablesByProjectId(projectId);

            if (tables.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "No tables found for project",
                    "projectId", projectId
                ));
            }

            int generatedCount = 0;
            for (TableSchema table : tables) {
                dynamicEndpointService.generateEndpointsForTable(table);
                generatedCount++;
            }

            return ResponseEntity.ok(Map.of(
                "message", "Endpoints regenerated successfully",
                "projectId", projectId,
                "tablesProcessed", generatedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to regenerate endpoints",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get a list of all tables that have generated endpoints
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTablesWithEndpoints() {
        try {
            Map<String, String> allDocs = dynamicEndpointService.getAllEndpointDocumentation();
            return ResponseEntity.ok(Map.of(
                "tables", allDocs.keySet(),
                "count", allDocs.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get API status and statistics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getEndpointStatus() {
        try {
            Map<String, String> allDocs = dynamicEndpointService.getAllEndpointDocumentation();
            List<TableSchema> allTables = tableService.getAllTables();

            return ResponseEntity.ok(Map.of(
                "totalTables", allTables.size(),
                "tablesWithEndpoints", allDocs.size(),
                "tablesWithoutEndpoints", allTables.size() - allDocs.size(),
                "status", "operational"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get status",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get structured endpoint documentation entity from MongoDB
     */
    @GetMapping("/docs/{tableName}/structured")
    public ResponseEntity<Map<String, Object>> getStructuredEndpointDocumentation(@PathVariable String tableName) {
        try {
            EndpointDocumentation doc = dynamicEndpointService.getEndpointDocumentationEntity(tableName);
            if (doc == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Documentation not found",
                    "tableName", tableName
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "tableName", tableName,
                "projectId", doc.getProjectId(),
                "documentation", doc,
                "createdAt", doc.getCreatedAt(),
                "updatedAt", doc.getUpdatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve structured documentation",
                "tableName", tableName,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get endpoint documentation by table and project from MongoDB
     */
    @GetMapping("/docs/{tableName}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getEndpointDocumentationByProject(
            @PathVariable String tableName,
            @PathVariable String projectId) {
        try {
            EndpointDocumentation doc = dynamicEndpointService.getEndpointDocumentationByTableAndProject(tableName, projectId);
            if (doc == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Documentation not found",
                    "tableName", tableName,
                    "projectId", projectId
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "tableName", tableName,
                "projectId", projectId,
                "documentation", doc
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve documentation",
                "tableName", tableName,
                "projectId", projectId,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get all endpoint documentation for a specific project
     */
    @GetMapping("/docs/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getAllEndpointDocumentationByProject(@PathVariable String projectId) {
        try {
            List<EndpointDocumentation> docs = dynamicEndpointService.getEndpointDocumentationByProject(projectId);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "projectId", projectId,
                "totalTables", docs.size(),
                "documentation", docs
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve project documentation",
                "projectId", projectId,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Remove endpoint documentation for specific table and project
     */

    @DeleteMapping("/docs/{tableName}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> removeEndpointDocumentationByProject(
            @PathVariable String tableName,
            @PathVariable String projectId) {
        try {
            dynamicEndpointService.removeEndpointsForTableAndProject(tableName, projectId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Endpoint documentation removed successfully",
                "tableName", tableName,
                "projectId", projectId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to remove endpoint documentation",
                "tableName", tableName,
                "projectId", projectId,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Parse plain text documentation into structured JSON format for frontend rendering
     */
    private Map<String, Object> parseDocumentationToJson(String docs, String tableName) {
        Map<String, Object> structuredDocs = new HashMap<>();

        try {
            String[] lines = docs.split("\n");
            Map<String, Object> tableInfo = new HashMap<>();
            List<Map<String, Object>> endpoints = new ArrayList<>();

            // Parse table information
            for (String line : lines) {
                if (line.startsWith("Table: ")) {
                    tableInfo.put("name", line.substring(7).trim());
                } else if (line.startsWith("Project ID: ")) {
                    tableInfo.put("projectId", line.substring(12).trim());
                } else if (line.startsWith("Schema: ")) {
                    tableInfo.put("schema", line.substring(8).trim());
                }
            }

            // Parse endpoints
            String currentEndpoint = null;
            Map<String, Object> endpointInfo = new HashMap<>();

            for (String line : lines) {
                line = line.trim();

                if (line.matches("\\d+\\. (GET|POST|PUT|DELETE) .*")) {
                    // Save previous endpoint if exists
                    if (currentEndpoint != null) {
                        endpoints.add(new HashMap<>(endpointInfo));
                    }

                    // Start new endpoint
                    String[] parts = line.split(" ", 3);
                    String method = parts[1];
                    String path = parts[2];

                    endpointInfo = new HashMap<>();
                    endpointInfo.put("method", method);
                    endpointInfo.put("path", path);
                    endpointInfo.put("fullUrl", "http://localhost:8080" + path);

                    currentEndpoint = method + " " + path;

                } else if (line.startsWith("Description: ")) {
                    endpointInfo.put("description", line.substring(13));

                } else if (line.startsWith("Response: ")) {
                    endpointInfo.put("responseType", line.substring(10));

                } else if (line.startsWith("Request Body: ")) {
                    endpointInfo.put("requestBody", line.substring(14));

                } else if (line.startsWith("Parameters: ")) {
                    endpointInfo.put("parameters", line.substring(12));

                } else if (line.startsWith("Example Schema: ")) {
                    endpointInfo.put("exampleSchema", line.substring(16));

                } else if (line.startsWith("Example: curl ")) {
                    endpointInfo.put("curlExample", line.substring(9));
                }
            }

            // Add last endpoint
            if (currentEndpoint != null) {
                endpoints.add(endpointInfo);
            }

            structuredDocs.put("tableInfo", tableInfo);
            structuredDocs.put("endpoints", endpoints);
            structuredDocs.put("totalEndpoints", endpoints.size());
            structuredDocs.put("basePath", "/api/tables/" + tableName);

        } catch (Exception e) {
            // Fallback to simple structure if parsing fails
            structuredDocs.put("rawDocumentation", docs);
            structuredDocs.put("error", "Failed to parse documentation: " + e.getMessage());
        }

        return structuredDocs;
    }
}
