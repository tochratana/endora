package rinsanom.com.springtwodatasoure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Table Management", description = "APIs for creating and managing dynamic tables")
@RestController
@RequestMapping("/table")
@RequiredArgsConstructor
public class CreateTableController {
    private final TableServiceImpl tableService;
    private final DynamicEndpointService dynamicEndpointService;

    @Operation(
        summary = "Create a new table",
        description = "Creates a new dynamic table with the specified schema in a project"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or table already exists")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTable(
            @Parameter(description = "Table creation request") @RequestBody CreateTableRequestDTO request) {
        try {
            tableService.createTablesWithUserValidation(request.getUserUuid(), request.getProjectUuid(), request.getSchemaName(), request.getSchema());
            return ResponseEntity.ok(Map.of(
                "message", "Table created successfully",
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", request.getUserUuid()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create table: " + e.getMessage(),
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid()
            ));
        }
    }

    @Operation(summary = "Get all tables", description = "Retrieves all tables from the system")
    @ApiResponse(responseCode = "200", description = "List of all tables")
    @GetMapping
    public List<TableSchema> getAllTables() {
        return tableService.getAllTables();
    }

    @Operation(
        summary = "Get tables by project",
        description = "Retrieves all tables belonging to a specific project"
    )
    @ApiResponse(responseCode = "200", description = "List of tables in the project")
    @GetMapping("/project/{projectUuid}")
    public List<TableSchema> getTablesByProject(
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        return tableService.getTablesByProjectId(projectUuid);
    }

    @Operation(
        summary = "Insert data with path parameters",
        description = "Inserts data into a table using path parameters for schema name, project UUID, and user UUID"
    )
    @PostMapping("/{schemaName}/project/{projectUuid}/user/{userUuid}/data")
    public ResponseEntity<Map<String, Object>> insertDataWithPathParams(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "User UUID") @PathVariable String userUuid,
            @Parameter(description = "Data to insert") @RequestBody Map<String, Object> data) {
        try {
            tableService.insertDataWithUserValidation(userUuid, schemaName, projectUuid, data);
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "schemaName", schemaName,
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

    @Operation(summary = "Get table by name and project", description = "Retrieves a specific table by its name and project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table found"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/{schemaName}/project/{projectUuid}")
    public TableSchema getTableByNameAndProject(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        return tableService.getTableByNameAndProject(schemaName, projectUuid);
    }

    @Operation(
        summary = "Insert data into table",
        description = "Inserts new data into a specified table"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data inserted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data format")
    })
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> insertData(
            @Parameter(description = "Data insertion request") @RequestBody InsertDataRequestDTO request) {
        try {
            tableService.insertDataWithUserValidation(request.getUserUuid(), request.getSchemaName(), request.getProjectUuid(), request.getData());
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "schemaName", request.getSchemaName(),
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

    @Operation(summary = "Get all data from table", description = "Retrieves all data from a specific table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/{schemaName}/data")
    public ResponseEntity<List<Map<String, Object>>> getTableData(@PathVariable String schemaName) {
        try {
            List<Map<String, Object>> data = tableService.getAllDataFromTable(schemaName);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Get table data by project", description = "Retrieves all data from a table within a specific project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table or project not found")
    })
    @GetMapping("/{schemaName}/data/project/{projectUuid}")
    public List<Map<String, Object>> getTableDataByProject(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        return tableService.getDataFromTableByProject(schemaName, projectUuid);
    }

    @Operation(summary = "Get record by ID", description = "Retrieves a specific record by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record found"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @GetMapping("/{schemaName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> getRecordById(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Record ID") @PathVariable String id,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {

        try {
            Map<String, Object> record = tableService.getRecordById(schemaName, id);
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

    @Operation(summary = "Update record by ID", description = "Updates a specific record by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data or record not found")
    })
    @PutMapping("/{schemaName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Record ID") @PathVariable String id,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "Updated data") @RequestBody Map<String, Object> data) {
        try {
            tableService.updateRecord(schemaName, id, data);
            return ResponseEntity.ok(Map.of(
                "message", "Record updated successfully",
                "schemaName", schemaName,
                "projectUuid", projectUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update record",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Delete record by ID", description = "Deletes a specific record by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @DeleteMapping("/{schemaName}/{id}/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            @PathVariable String schemaName,
            @PathVariable String id,
            @PathVariable String projectUuid) {
        try {
            tableService.deleteRecord(schemaName, id);
            return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "schemaName", schemaName,
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

    @Operation(summary = "Get related tables", description = "Retrieves tables that are related to the specified table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Related tables found"),
        @ApiResponse(responseCode = "404", description = "Table or project not found")
    })
    @GetMapping("/{schemaName}/relationships/project/{projectUuid}")
    public ResponseEntity<List<TableSchema>> getRelatedTables(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        return ResponseEntity.ok(tableService.getRelatedTables(schemaName, projectUuid));
    }

    @Operation(summary = "Get table documentation", description = "Retrieves API documentation for a specific table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documentation found"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/{schemaName}/docs")
    public ResponseEntity<String> getTableDocumentation(
            @Parameter(description = "Schema name") @PathVariable String schemaName) {
        try {
            String docs = dynamicEndpointService.getEndpointDocumentation(schemaName);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all documentation", description = "Retrieves all generated API documentation")
    @ApiResponse(responseCode = "200", description = "All documentation retrieved")
    @GetMapping("/docs")
    public ResponseEntity<Map<String, String>> getAllDocumentation() {
        Map<String, String> allDocs = dynamicEndpointService.getAllEndpointDocumentation();
        return ResponseEntity.ok(allDocs);
    }

    @Operation(summary = "Get record with relations", description = "Retrieves a record with all its related data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record with relations found"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @GetMapping("/{schemaName}/{id}/with-relations/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> getRecordWithRelations(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Record ID") @PathVariable String id,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        try {
            Map<String, Object> recordWithRelations = tableService.getRecordWithRelations(schemaName, id, projectUuid);
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

    @Operation(summary = "Debug project tables", description = "Debug endpoint to troubleshoot project tables and relationships")
    @ApiResponse(responseCode = "200", description = "Debug information returned")
    @GetMapping("/debug/project/{projectUuid}")
    public ResponseEntity<Map<String, Object>> debugProjectTables(
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        try {
            List<TableSchema> allTables = tableService.getTablesByProjectId(projectUuid);

            // Log debug information to server console only
            System.out.println("DEBUG: Project " + projectUuid + " has " + allTables.size() + " tables");
            for (TableSchema table : allTables) {
                System.out.println("DEBUG: Table '" + table.getSchemaName() + "' has " +
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

    @Operation(
        summary = "Create table with relationships",
        description = "Creates a new table with defined relationships to other tables"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table with relationships created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or table already exists")
    })
    @PostMapping("/with-relationships")
    public ResponseEntity<Map<String, Object>> createTableWithRelationships(
            @Parameter(description = "Table creation request with relationships") @RequestBody CreateTableWithRelationshipsDTO request) {
        try {
            tableService.createTableWithRelationshipsAndUserValidation(
                request.getUserUuid(),
                request.getProjectUuid(),
                request.getSchemaName(),
                request.getSchema(),
                request.getRelationships()
            );
            return ResponseEntity.ok(Map.of(
                "message", "Table with relationships created successfully",
                "schemaName", request.getSchemaName(),
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
}
