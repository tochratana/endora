package rinsanom.com.springtwodatasoure.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.dto.CreateTableRequestDTO;
import rinsanom.com.springtwodatasoure.dto.CreateTableWithRelationshipsDTO;
import rinsanom.com.springtwodatasoure.dto.InsertDataRequestDTO;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;
import rinsanom.com.springtwodatasoure.service.impl.TableServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/table")
@RequiredArgsConstructor
public class CreateTableController {
    private final TableServiceImpl tableService;
    private final DynamicEndpointService dynamicEndpointService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTable(@RequestBody CreateTableRequestDTO request) {
        try {
            tableService.createTablesWithUserValidation(request.getUserUuid(), request.getProjectUuid(), request.getTableName(), request.getSchema());
            return ResponseEntity.ok(Map.of(
                "message", "Table created successfully",
                "tableName", request.getTableName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", request.getUserUuid()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create table",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public List<TableSchema> getAllTables() {
        return tableService.getAllTables();
    }

    @GetMapping("/project/{projectUuid}")
    public List<TableSchema> getTablesByProject(@PathVariable String projectUuid) {
        return tableService.getTablesByProjectId(projectUuid);
    }

    @GetMapping("/{tableName}/project/{projectUuid}")
    public TableSchema getTableByNameAndProject(@PathVariable String tableName, @PathVariable String projectUuid) {
        return tableService.getTableByNameAndProject(tableName, projectUuid);
    }

    // Updated data operations with user validation
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> insertData(@RequestBody InsertDataRequestDTO request) {
        try {
            tableService.insertDataWithUserValidation(request.getUserUuid(), request.getTableName(), request.getProjectUuid(), request.getData());
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "tableName", request.getTableName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", request.getUserUuid()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to insert data",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{tableName}/data/project/{projectUuid}/user/{userUuid}")
    public ResponseEntity<Map<String, Object>> insertDataWithProject(
            @PathVariable String tableName,
            @PathVariable String projectUuid,
            @PathVariable String userUuid,
            @RequestBody Map<String, Object> data) {
        try {
            tableService.insertDataWithUserValidation(userUuid, tableName, projectUuid, data);
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "tableName", tableName,
                "projectUuid", projectUuid,
                "userUuid", userUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to insert data",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{tableName}/data")
    public ResponseEntity<List<Map<String, Object>>> getTableData(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> data = tableService.getAllDataFromTable(tableName);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{tableName}/data/project/{projectUuid}")
    public List<Map<String, Object>> getTableDataByProject(@PathVariable String tableName, @PathVariable String projectUuid) {
        return tableService.getDataFromTableByProject(tableName, projectUuid);
    }

    // Get a specific record by ID
    @GetMapping("/{tableName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> getRecordById(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectUuid) {
        try {
            Map<String, Object> record = tableService.getRecordById(tableName, id);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get record",
                "message", e.getMessage()
            ));
        }
    }

    // Update a specific record by ID
    @PutMapping("/{tableName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectUuid,
            @RequestBody Map<String, Object> data) {
        try {
            tableService.updateRecord(tableName, id, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record updated successfully",
                "tableName", tableName,
                "id", id,
                "projectUuid", projectUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update record",
                "message", e.getMessage()
            ));
        }
    }

    // Delete a specific record by ID
    @DeleteMapping("/{tableName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectUuid) {
        try {
            tableService.deleteRecord(tableName, id);
            return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "tableName", tableName,
                "id", id,
                "projectUuid", projectUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete record",
                "message", e.getMessage()
            ));
        }
    }

    // New endpoint to get API documentation for a specific table
    @GetMapping("/{tableName}/docs")
    public ResponseEntity<String> getTableDocumentation(@PathVariable String tableName) {
        try {
            String docs = dynamicEndpointService.getEndpointDocumentation(tableName);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // New endpoint to get all generated API documentation
    @GetMapping("/docs")
    public ResponseEntity<Map<String, String>> getAllDocumentation() {
        Map<String, String> allDocs = dynamicEndpointService.getAllEndpointDocumentation();
        return ResponseEntity.ok(allDocs);
    }

    // UPDATED ENDPOINTS FOR TABLE RELATIONSHIPS WITH USER VALIDATION
    @PostMapping("/with-relationships")
    public ResponseEntity<Map<String, Object>> createTableWithRelationships(@RequestBody CreateTableWithRelationshipsDTO request) {
        try {
            tableService.createTableWithRelationshipsAndUserValidation(
                request.getUserUuid(),
                request.getProjectUuid(),
                request.getTableName(),
                request.getSchema(),
                request.getRelationships()
            );
            return ResponseEntity.ok(Map.of(
                "message", "Table with relationships created successfully",
                "tableName", request.getTableName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", request.getUserUuid(),
                "relationships", request.getRelationships() != null ? request.getRelationships().size() : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create table with relationships",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{tableName}/relationships/project/{projectUuid}")
    public ResponseEntity<List<TableSchema>> getRelatedTables(@PathVariable String tableName, @PathVariable String projectUuid) {
        try {
            List<TableSchema> relatedTables = tableService.getRelatedTables(projectUuid, tableName);
            return ResponseEntity.ok(relatedTables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // DEBUG ENDPOINT - Add this to help troubleshoot
    @GetMapping("/debug/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> debugProjectTables(@PathVariable String projectUuid) {
        try {
            List<TableSchema> allTables = tableService.getTablesByProjectId(projectUuid);

            // Log debug information to server console only
            System.out.println("DEBUG: Project " + projectUuid + " has " + allTables.size() + " tables");
            for (TableSchema table : allTables) {
                System.out.println("DEBUG: Table '" + table.getTableName() + "' has " +
                    (table.getRelationships() != null ? table.getRelationships().size() : 0) + " relationships");

                if (table.getRelationships() != null) {
                    for (TableSchema.TableRelationship rel : table.getRelationships()) {
                        System.out.println("DEBUG: - " + rel.getForeignKeyColumn() + " -> " +
                            rel.getReferencedTable() + "." + rel.getReferencedColumn());
                    }
                }
            }

            // Return minimal response to frontend
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Debug information logged to server console");
            response.put("tableCount", allTables.size());
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get debug info",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{tableName}/{id}/with-relations/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> getRecordWithRelations(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectUuid) {
        try {
            Map<String, Object> recordWithRelations = tableService.getRecordWithRelations(tableName, id, projectUuid);
            if (recordWithRelations == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(recordWithRelations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get record with relations",
                "message", e.getMessage()
            ));
        }
    }
}
