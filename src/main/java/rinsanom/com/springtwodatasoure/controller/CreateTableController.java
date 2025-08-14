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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/table")
@RequiredArgsConstructor
public class CreateTableController {
    private final TableServiceImpl tableService;
    private final DynamicEndpointService dynamicEndpointService;

    @PostMapping
    public void createTable(@RequestBody CreateTableRequestDTO request) {
        tableService.createTables(request.getProjectId(), request.getTableName(), request.getSchema());
    }

    @GetMapping
    public List<TableSchema> getAllTables() {
        return tableService.getAllTables();
    }

    @GetMapping("/project/{projectId}")
    public List<TableSchema> getTablesByProject(@PathVariable String projectId) {
        return tableService.getTablesByProjectId(projectId);
    }

    @GetMapping("/{tableName}/project/{projectId}")
    public TableSchema getTableByNameAndProject(@PathVariable String tableName, @PathVariable String projectId) {
        return tableService.getTableByNameAndProject(tableName, projectId);
    }

    // New endpoints for data operations with project support
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> insertData(@RequestBody InsertDataRequestDTO request) {
        try {
            tableService.insertData(request.getTableName(), request.getData());
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "tableName", request.getTableName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to insert data",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{tableName}/data/project/{projectId}")
    public ResponseEntity<Map<String, Object>> insertDataWithProject(
            @PathVariable String tableName,
            @PathVariable String projectId,
            @RequestBody Map<String, Object> data) {
        try {
            // Add projectId to data map
            data.put("projectId", projectId);
            tableService.insertData(tableName, data);
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "tableName", tableName,
                "projectId", projectId
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

    @GetMapping("/{tableName}/data/project/{projectId}")
    public List<Map<String, Object>> getTableDataByProject(@PathVariable String tableName, @PathVariable String projectId) {
        return tableService.getDataFromTableByProject(tableName, projectId);
    }

    // Get a specific record by ID
    @GetMapping("/{tableName}/{id}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getRecordById(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectId) {
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
    @PutMapping("/{tableName}/{id}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectId,
            @RequestBody Map<String, Object> data) {
        try {
            tableService.updateRecord(tableName, id, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record updated successfully",
                "tableName", tableName,
                "id", id,
                "projectId", projectId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update record",
                "message", e.getMessage()
            ));
        }
    }

    // Delete a specific record by ID
    @DeleteMapping("/{tableName}/{id}/project/{projectId}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectId) {
        try {
            tableService.deleteRecord(tableName, id);
            return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "tableName", tableName,
                "id", id,
                "projectId", projectId
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

    // NEW ENDPOINTS FOR TABLE RELATIONSHIPS

    @PostMapping("/with-relationships")
    public ResponseEntity<Map<String, Object>> createTableWithRelationships(@RequestBody CreateTableWithRelationshipsDTO request) {
        try {
            tableService.createTableWithRelationships(request.getProjectId(), request.getTableName(), request.getSchema(), request.getRelationships());
            return ResponseEntity.ok(Map.of(
                "message", "Table with relationships created successfully",
                "tableName", request.getTableName(),
                "projectId", request.getProjectId(),
                "relationships", request.getRelationships() != null ? request.getRelationships().size() : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create table with relationships",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{tableName}/relationships/project/{projectId}")
    public ResponseEntity<List<TableSchema>> getRelatedTables(@PathVariable String tableName, @PathVariable String projectId) {
        try {
            List<TableSchema> relatedTables = tableService.getRelatedTables(projectId, tableName);
            return ResponseEntity.ok(relatedTables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{tableName}/{id}/with-relations/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getRecordWithRelations(
            @PathVariable String tableName,
            @PathVariable String id,
            @PathVariable String projectId) {
        try {
            Map<String, Object> recordWithRelations = tableService.getRecordWithRelations(tableName, id, projectId);
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
