package rinsanom.com.springtwodatasoure.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rinsanom.com.springtwodatasoure.service.TableService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client-api/{projectId}")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {

    private final TableService tableService;

    @GetMapping("/{tableName}")
    public ResponseEntity<?> getData(@PathVariable String projectId,
                                     @PathVariable String tableName,
                                     HttpServletRequest request) {
        try {
            // User info is available if authentication was required
            Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");

            List<Map<String, Object>> data = tableService.getDataFromTableByProject(tableName, projectId);

            Map<String, Object> response = Map.of(
                    "data", data,
                    "count", data.size()
            );

            if (user != null) {
                ((Map<String, Object>) response).put("authenticatedUser", user.get("email"));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get data from table {}: {}", tableName, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to retrieve data: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{tableName}")
    public ResponseEntity<?> createData(@PathVariable String projectId,
                                        @PathVariable String tableName,
                                        @RequestBody Map<String, Object> data,
                                        HttpServletRequest request) {
        try {
            Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");

            // Add user context to data if authenticated
            if (user != null) {
                data.put("created_by", user.get("id"));
            }

            tableService.insertData(tableName, projectId, data);

            return ResponseEntity.ok(Map.of(
                    "message", "Data created successfully",
                    "tableName", tableName,
                    "projectId", projectId
            ));

        } catch (Exception e) {
            log.error("Failed to create data in table {}: {}", tableName, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to create data: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{tableName}/{id}")
    public ResponseEntity<?> getDataById(@PathVariable String projectId,
                                         @PathVariable String tableName,
                                         @PathVariable String id,
                                         HttpServletRequest request) {
        try {
            Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");

            Map<String, Object> record = tableService.getRecordWithRelations(tableName, id, projectId);

            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(record);

        } catch (Exception e) {
            log.error("Failed to get record by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to retrieve record: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{tableName}/{id}")
    public ResponseEntity<?> updateData(@PathVariable String projectId,
                                        @PathVariable String tableName,
                                        @PathVariable String id,
                                        @RequestBody Map<String, Object> data,
                                        HttpServletRequest request) {
        try {
            Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");

            // Add user context
            if (user != null) {
                data.put("updated_by", user.get("id"));
            }

            tableService.updateRecord(tableName, id, data);

            return ResponseEntity.ok(Map.of(
                    "message", "Record updated successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to update record: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to update record: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{tableName}/{id}")
    public ResponseEntity<?> deleteData(@PathVariable String projectId,
                                        @PathVariable String tableName,
                                        @PathVariable String id,
                                        HttpServletRequest request) {
        try {
            tableService.deleteRecord(tableName, id);

            return ResponseEntity.ok(Map.of(
                    "message", "Record deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to delete record: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to delete record: " + e.getMessage()
            ));
        }
    }
}