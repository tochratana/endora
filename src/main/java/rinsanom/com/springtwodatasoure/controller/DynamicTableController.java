package rinsanom.com.springtwodatasoure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class DynamicTableController {

    private final TableService tableService;

    // GET /api/tables/{tableName} - Get all records from a specific table
    @GetMapping("/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> getAllRecords(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> records = tableService.getAllDataFromTable(tableName);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/tables/{tableName} - Create a new record in a specific table
    @PostMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> createRecord(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> data) {
        try {
            // Extract projectId from data payload
            String projectId = (String) data.get("projectId");
            if (projectId == null || projectId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "projectId is required in the request body"
                ));
            }

            tableService.insertData(tableName, projectId, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record created successfully",
                "table", tableName,
                "projectId", projectId,
                "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create record",
                "message", e.getMessage()
            ));
        }
    }

    // GET /api/tables/{tableName}/{id} - Get a specific record by ID
    @GetMapping("/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> getRecordById(
            @PathVariable String tableName,
            @PathVariable String id) {
        try {
            Map<String, Object> record = tableService.getRecordById(tableName, id);
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

    // PUT /api/tables/{tableName}/{id} - Update a specific record by ID
    @PutMapping("/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable String tableName,
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        try {
            tableService.updateRecord(tableName, id, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record updated successfully",
                "table", tableName,
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

    // DELETE /api/tables/{tableName}/{id} - Delete a specific record by ID
    @DeleteMapping("/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            @PathVariable String tableName,
            @PathVariable String id) {
        try {
            tableService.deleteRecord(tableName, id);
            return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "table", tableName,
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
