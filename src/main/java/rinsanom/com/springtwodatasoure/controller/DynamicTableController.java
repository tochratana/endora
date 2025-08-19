package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class DynamicTableController {

    private final TableService tableService;

    // GET /api/tables/{schemaName} - Get all records from a specific table
    @GetMapping("/{schemaName}")
    public ResponseEntity<List<Map<String, Object>>> getAllRecords(@PathVariable String schemaName) {
        try {
            List<Map<String, Object>> records = tableService.getAllDataFromTable(schemaName);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/tables/{schemaName} - Create a new record in a specific table
    @PostMapping("/{schemaName}")
    public ResponseEntity<Map<String, Object>> createRecord(
            @PathVariable String schemaName,
            @RequestBody Map<String, Object> data) {
        try {
            // Extract projectId from data payload
            String projectId = (String) data.get("projectId");
            if (projectId == null || projectId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "projectId is required in the request body"
                ));
            }

            tableService.insertData(schemaName, projectId, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record created successfully",
                "table", schemaName,
                "projectId", projectId,
                "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create record: " + e.getMessage()
            ));
        }
    }

    // GET /api/tables/{schemaName}/{id} - Get a specific record by ID
    @GetMapping("/{schemaName}/{id}")
    public ResponseEntity<Map<String, Object>> getRecordById(
            @PathVariable String schemaName,
            @PathVariable String id) {
        try {
            Map<String, Object> record = tableService.getRecordById(schemaName, id);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to retrieve record",
                "message", e.getMessage()
            ));
        }
    }

    // PUT /api/tables/{schemaName}/{id} - Update a specific record by ID
    @PutMapping("/{schemaName}/{id}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable String schemaName,
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        try {
            tableService.updateRecord(schemaName, id, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record updated successfully",
                "table", schemaName,
                "id", id,
                "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update record",
                "message", e.getMessage()
            ));
        }
    }

    // DELETE /api/tables/{schemaName}/{id} - Delete a specific record by ID
    @DeleteMapping("/{schemaName}/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            @PathVariable String schemaName,
            @PathVariable String id) {
        try {
            tableService.deleteRecord(schemaName, id);
            return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "table", schemaName,
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete record",
                "message", e.getMessage()
            ));
        }
    }
}
