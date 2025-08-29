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
import rinsanom.com.springtwodatasoure.security.TokenUserService;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;
import rinsanom.com.springtwodatasoure.service.impl.TableServiceImpl;

import java.util.List;
import java.util.Map;

@Tag(name = "Table Management", description = "APIs for creating and managing dynamic tables")
@RestController
@RequestMapping("/table")
@RequiredArgsConstructor
public class CreateTableController {
    private final TableServiceImpl tableService;
    private final DynamicEndpointService dynamicEndpointService;
    private final TokenUserService tokenUserService;

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
            // Extract user UUID from JWT token using centralized service
            String userUuid = tokenUserService.getCurrentUserUuid();

            tableService.createTablesWithUserValidation(userUuid, request.getProjectUuid(), request.getSchemaName(), request.getSchema());
            return ResponseEntity.ok(Map.of(
                "message", "Table created successfully",
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", userUuid
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
        summary = "Create a table with relationships",
        description = "Creates a new table with schema and relationships in a single request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table with relationships created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or table already exists")
    })
    @PostMapping("/with-relationships")
    public ResponseEntity<Map<String, Object>> createTableWithRelationships(
            @Parameter(description = "Table creation request with relationships") @RequestBody CreateTableWithRelationshipsDTO request) {
        try {
            // Extract user UUID from JWT token using centralized service
            String userUuid = tokenUserService.getCurrentUserUuid();

            tableService.createTableWithRelationshipsAndUserValidation(
                userUuid,
                request.getProjectUuid(),
                request.getSchemaName(),
                request.getSchema(),
                request.getRelationships()
            );
            return ResponseEntity.ok(Map.of(
                "message", "Table with relationships created successfully",
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", userUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create table with relationships: " + e.getMessage(),
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid()
            ));
        }
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
            // Validate that the current user can access this user's data
            tokenUserService.validateUserAccess(userUuid);

            tableService.insertDataWithUserValidation(userUuid, schemaName, projectUuid, data);
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "userUuid", userUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to insert data: " + e.getMessage(),
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "userUuid", userUuid
            ));
        }
    }

    @Operation(
        summary = "Insert data into current user's table",
        description = "Inserts data into a table for the current authenticated user"
    )
    @PostMapping("/{schemaName}/project/{projectUuid}/data")
    public ResponseEntity<Map<String, Object>> insertDataForCurrentUser(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "Data to insert") @RequestBody Map<String, Object> data) {
        try {
            // Get current user's UUID from token
            String userUuid = tokenUserService.getCurrentUserUuid();

            tableService.insertDataWithUserValidation(userUuid, schemaName, projectUuid, data);
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "userUuid", userUuid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to insert data: " + e.getMessage(),
                "schemaName", schemaName,
                "projectUuid", projectUuid
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
            // Extract user UUID from JWT token using centralized service
            String userUuid = tokenUserService.getCurrentUserUuid();

            tableService.insertDataWithUserValidation(userUuid, request.getSchemaName(), request.getProjectUuid(), request.getData());
            return ResponseEntity.ok(Map.of(
                "message", "Data inserted successfully",
                "schemaName", request.getSchemaName(),
                "projectUuid", request.getProjectUuid(),
                "userUuid", userUuid
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

    // Enhanced Relationship Management Endpoints

    @Operation(
        summary = "Get table relationships",
        description = "Retrieves all relationships defined for a specific table"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relationships retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/{schemaName}/project/{projectUuid}/relationships")
    public ResponseEntity<Map<String, Object>> getTableRelationships(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        try {
            List<TableSchema.TableRelationship> relationships = tableService.getTableRelationships(schemaName, projectUuid);
            return ResponseEntity.ok(Map.of(
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "relationships", relationships,
                "count", relationships.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get table relationships",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Add relationship to table",
        description = "Adds a new foreign key relationship to an existing table"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relationship added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid relationship or table not found")
    })
    @PostMapping("/{schemaName}/project/{projectUuid}/relationships")
    public ResponseEntity<Map<String, Object>> addRelationshipToTable(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "Relationship to add") @RequestBody CreateTableWithRelationshipsDTO.TableRelationship relationship) {
        try {
            tableService.addRelationshipToTable(schemaName, projectUuid, relationship);
            return ResponseEntity.ok(Map.of(
                "message", "Relationship added successfully",
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "foreignKeyColumn", relationship.getForeignKeyColumn(),
                "referencedTable", relationship.getReferencedTable()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add relationship to table",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Remove relationship from table",
        description = "Removes a foreign key relationship from a table"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relationship removed successfully"),
        @ApiResponse(responseCode = "404", description = "Table or relationship not found")
    })
    @DeleteMapping("/{schemaName}/project/{projectUuid}/relationships/{foreignKeyColumn}")
    public ResponseEntity<Map<String, Object>> removeRelationshipFromTable(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "Foreign key column name") @PathVariable String foreignKeyColumn) {
        try {
            tableService.removeRelationshipFromTable(schemaName, projectUuid, foreignKeyColumn);
            return ResponseEntity.ok(Map.of(
                "message", "Relationship removed successfully",
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "foreignKeyColumn", foreignKeyColumn
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to remove relationship from table",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Get records with joins",
        description = "Retrieves records from a table with joined data from related tables"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Records with joins retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @PostMapping("/{schemaName}/project/{projectUuid}/records-with-joins")
    public ResponseEntity<Map<String, Object>> getRecordsWithJoins(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "List of tables to join") @RequestBody List<String> joinTables) {
        try {
            List<Map<String, Object>> records = tableService.getRecordsWithJoins(schemaName, projectUuid, joinTables);
            return ResponseEntity.ok(Map.of(
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "joinTables", joinTables,
                "records", records,
                "count", records.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get records with joins",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Get record with relations",
        description = "Retrieves a specific record with all its related data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record with relations retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @GetMapping("/{schemaName}/project/{projectUuid}/record/{id}/with-relations")
    public ResponseEntity<Map<String, Object>> getRecordWithRelations(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid,
            @Parameter(description = "Record ID") @PathVariable String id) {
        try {
            Map<String, Object> record = tableService.getRecordWithRelations(schemaName, id, projectUuid);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of(
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "recordId", id,
                "record", record
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get record with relations",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Get related tables",
        description = "Retrieves all tables that have foreign key relationships to the specified table"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Related tables retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/{schemaName}/project/{projectUuid}/related-tables")
    public ResponseEntity<Map<String, Object>> getRelatedTables(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        try {
            List<TableSchema> relatedTables = tableService.getRelatedTables(projectUuid, schemaName);
            return ResponseEntity.ok(Map.of(
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "relatedTables", relatedTables,
                "count", relatedTables.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get related tables",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Validate relationship integrity",
        description = "Validates that all foreign key relationships in a table maintain referential integrity"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @GetMapping("/{schemaName}/project/{projectUuid}/validate-integrity")
    public ResponseEntity<Map<String, Object>> validateRelationshipIntegrity(
            @Parameter(description = "Schema name") @PathVariable String schemaName,
            @Parameter(description = "Project UUID") @PathVariable String projectUuid) {
        try {
            boolean isValid = tableService.validateRelationshipIntegrity(schemaName, projectUuid);
            return ResponseEntity.ok(Map.of(
                "schemaName", schemaName,
                "projectUuid", projectUuid,
                "isValid", isValid,
                "message", isValid ? "All relationships are valid" : "Referential integrity violations found"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to validate relationship integrity",
                "message", e.getMessage()
            ));
        }
    }
}
