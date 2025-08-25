package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.dto.CreateTableWithRelationshipsDTO;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.entity.TableData;
import rinsanom.com.springtwodatasoure.entity.Projects;
import rinsanom.com.springtwodatasoure.repository.mongo.TableSchemaRepository;
import rinsanom.com.springtwodatasoure.repository.mongo.TableDataRepository;
import rinsanom.com.springtwodatasoure.repository.mongo.ProjectRepository;
import rinsanom.com.springtwodatasoure.repository.postgrest.UserRepository;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableServiceImpl implements TableService {
    private final TableSchemaRepository tableSchemaRepository;
    private final TableDataRepository tableDataRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DynamicEndpointService dynamicEndpointService;

    @Override
    public void createTables(String projectUuid, String schemaName, Map<String, String> schema) {
        createTablesWithUserValidation(null, projectUuid, schemaName, schema);
    }

    // New method that includes user validation
    public void createTablesWithUserValidation(String userUuid, String projectUuid, String schemaName, Map<String, String> schema) {
        try {
            // Validate user exists (if userUuid provided)
            if (userUuid != null) {
                userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User with UUID " + userUuid + " not found"));
            }

            // Validate project exists and belongs to user (if userUuid provided)
            Optional<Projects> projectOpt = projectRepository.findByProjectUuid(projectUuid);
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project with UUID " + projectUuid + " not found");
            }

            Projects project = projectOpt.get();

            // Check if user owns the project (if userUuid provided)
            if (userUuid != null && !project.getUserUuid().equals(userUuid)) {
                throw new RuntimeException("User " + userUuid + " does not have permission to create tables in project " + projectUuid);
            }

            // Check if table already exists in this project
            Optional<TableSchema> existingTable = tableSchemaRepository.findBySchemaNameAndProjectId(schemaName, project.getProjectUuid());
            if (existingTable.isPresent()) {
                throw new RuntimeException("Table '" + schemaName + "' already exists in project " + projectUuid);
            }

            // Create table schema
            TableSchema tableSchema = new TableSchema(schemaName, project.getProjectUuid(), schema);
            tableSchemaRepository.save(tableSchema);

            // Generate endpoints automatically
            dynamicEndpointService.generateEndpointsForTable(tableSchema);

            log.info("Table '{}' created successfully in project '{}' by user '{}'", schemaName, projectUuid, userUuid);

        } catch (Exception e) {
            log.error("Failed to create table '{}' in project '{}': {}", schemaName, projectUuid, e.getMessage());
            throw new RuntimeException("Failed to create table: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropTables() {

    }

    @Override
    public void createMongoCollection() {

    }

    @Override
    public void dropMongoCollection() {

    }

    @Override
    public List<TableSchema> getAllTables() {
        return tableSchemaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<TableSchema> getTablesByProjectId(String projectUuid) {
        return tableSchemaRepository.findByProjectId(projectUuid);
    }

    @Override
    public TableSchema getTableByNameAndProject(String schemaName, String projectUuid) {
        return tableSchemaRepository.findBySchemaNameAndProjectId(schemaName, projectUuid)
                .orElse(null);
    }

    @Override
    public void insertData(String schemaName, String projectUuid, Map<String, Object> data) {
        insertDataWithUserValidation(null, schemaName, projectUuid, data);
    }

    // New method that includes user validation
    public void insertDataWithUserValidation(String userUuid, String schemaName, String projectUuid, Map<String, Object> data) {
        try {
            log.info("Attempting to insert data into table: {}, projectUuid: {}", schemaName, projectUuid);

            if (projectUuid == null) {
                throw new RuntimeException("Project UUID is required when inserting data");
            }

            // Validate user exists (if userUuid provided)
            if (userUuid != null) {
                userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User with UUID " + userUuid + " not found"));
            }

            // Validate project exists and belongs to user (if userUuid provided)
            Optional<Projects> projectOpt = projectRepository.findByProjectUuid(projectUuid);
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project with UUID " + projectUuid + " not found");
            }

            Projects project = projectOpt.get();
            if (userUuid != null && !project.getUserUuid().equals(userUuid)) {
                throw new RuntimeException("Project " + projectUuid + " does not belong to user " + userUuid);
            }

            // Verify table schema exists
            TableSchema tableSchema = getTableByNameAndProject(schemaName, projectUuid);
            if (tableSchema == null) {
                throw new RuntimeException("Table '" + schemaName + "' does not exist in project " + projectUuid);
            }

            // Create new table data document in MongoDB
            TableData tableData = new TableData(schemaName, projectUuid, data);
            tableDataRepository.save(tableData);

            System.out.println("Data inserted successfully into table '" + schemaName + "' for project " + projectUuid);

        } catch (Exception e) {
            System.err.println("Error inserting data: " + e.getMessage());
            throw new RuntimeException("Failed to insert data: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllDataFromTable(String schemaName) {
        try {
            List<TableData> tableDataList = tableDataRepository.findBySchemaName(schemaName);
            return tableDataList.stream()
                    .map(tableData -> {
                        Map<String, Object> dataWithId = new HashMap<>(tableData.getData());
                        dataWithId.put("id", tableData.getId());
                        dataWithId.put("createdAt", tableData.getCreatedAt());
                        dataWithId.put("updatedAt", tableData.getUpdatedAt());
                        return dataWithId;
                    })
                    .toList();
        } catch (Exception e) {
            System.err.println("Error retrieving data: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve data from table: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getDataFromTableByProject(String schemaName, String projectUuid) {
        try {
            List<TableData> tableDataList = tableDataRepository.findBySchemaNameAndProjectId(schemaName, projectUuid);
            return tableDataList.stream()
                    .map(tableData -> {
                        Map<String, Object> dataWithId = new HashMap<>(tableData.getData());
                        dataWithId.put("id", tableData.getId());
                        dataWithId.put("createdAt", tableData.getCreatedAt());
                        dataWithId.put("updatedAt", tableData.getUpdatedAt());
                        return dataWithId;
                    })
                    .toList();
        } catch (Exception e) {
            System.err.println("Error retrieving data by project: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve data from table by project: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRecordById(String schemaName, String id) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getSchemaName().equals(schemaName)) {
                return null;
            }

            TableData tableData = tableDataOpt.get();
            Map<String, Object> dataWithId = new HashMap<>(tableData.getData());
            dataWithId.put("id", tableData.getId());
            dataWithId.put("createdAt", tableData.getCreatedAt());
            dataWithId.put("updatedAt", tableData.getUpdatedAt());
            return dataWithId;
        } catch (Exception e) {
            System.err.println("Error retrieving record by ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve record: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateRecord(String schemaName, String id, Map<String, Object> data) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getSchemaName().equals(schemaName)) {
                throw new RuntimeException("No record found with ID: " + id + " in table: " + schemaName);
            }

            TableData tableData = tableDataOpt.get();

            // Update the data fields
            Map<String, Object> updatedData = new HashMap<>(tableData.getData());
            updatedData.putAll(data);

            tableData.setData(updatedData);
            tableData.updateTimestamp();

            tableDataRepository.save(tableData);

            System.out.println("Record updated successfully in table '" + schemaName + "' with ID: " + id);

        } catch (Exception e) {
            System.err.println("Error updating record: " + e.getMessage());
            throw new RuntimeException("Failed to update record: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRecord(String schemaName, String id) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getSchemaName().equals(schemaName)) {
                throw new RuntimeException("No record found with ID: " + id + " in table: " + schemaName);
            }

            tableDataRepository.deleteById(id);

            System.out.println("Record deleted successfully from table '" + schemaName + "' with ID: " + id);

        } catch (Exception e) {
            System.err.println("Error deleting record: " + e.getMessage());
            throw new RuntimeException("Failed to delete record: " + e.getMessage(), e);
        }
    }

    @Override
    public void createTableWithRelationships(String projectUuid, String schemaName, Map<String, String> schema,
                                           List<CreateTableWithRelationshipsDTO.TableRelationship> relationships) {
        createTableWithRelationshipsAndUserValidation(null, projectUuid, schemaName, schema, relationships);
    }

    // New method that includes user validation
    public void createTableWithRelationshipsAndUserValidation(String userUuid, String projectUuid, String schemaName,
                                                             Map<String, String> schema,
                                                             List<CreateTableWithRelationshipsDTO.TableRelationship> relationships) {
        try {
            // Validate user exists (if userUuid provided)
            if (userUuid != null) {
                userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User with UUID " + userUuid + " not found"));
            }

            // Validate project exists and belongs to user (if userUuid provided)
            Optional<Projects> projectOpt = projectRepository.findByProjectUuid(projectUuid);
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project with UUID " + projectUuid + " not found");
            }

            Projects project = projectOpt.get();
            if (userUuid != null && !project.getUserUuid().equals(userUuid)) {
                throw new RuntimeException("Project " + projectUuid + " does not belong to user " + userUuid);
            }

            // Check if table schema already exists in MongoDB
            Optional<TableSchema> existingTable = tableSchemaRepository.findBySchemaNameAndProjectId(schemaName, projectUuid);

            if (existingTable.isPresent()) {
                throw new RuntimeException("Table '" + schemaName + "' already exists in project " + projectUuid);
            }

            // Validate that referenced tables exist in the same project
            if (relationships != null) {
                for (CreateTableWithRelationshipsDTO.TableRelationship rel : relationships) {
                    TableSchema referencedTable = getTableByNameAndProject(rel.getReferencedTable(), projectUuid);
                    if (referencedTable == null) {
                        throw new RuntimeException("Referenced table '" + rel.getReferencedTable() + "' does not exist in project " + projectUuid);
                    }
                }
            }

            // Create table schema with relationships (no PostgreSQL table creation)
            TableSchema tableSchema = new TableSchema(schemaName, projectUuid, schema);

            // Convert DTO relationships to entity relationships
            List<TableSchema.TableRelationship> entityRelationships = null;
            if (relationships != null) {
                entityRelationships = relationships.stream()
                        .map(rel -> {
                            TableSchema.TableRelationship entityRel = new TableSchema.TableRelationship();
                            entityRel.setForeignKeyColumn(rel.getForeignKeyColumn());
                            entityRel.setReferencedTable(rel.getReferencedTable());
                            entityRel.setReferencedColumn(rel.getReferencedColumn());
                            entityRel.setOnDelete(rel.getOnDelete());
                            entityRel.setOnUpdate(rel.getOnUpdate());
                            return entityRel;
                        })
                        .toList();
            }

            tableSchema.setRelationships(entityRelationships);
            tableSchemaRepository.save(tableSchema);

            // AUTO-GENERATE ENDPOINTS FOR THE NEW TABLE
            dynamicEndpointService.generateEndpointsForTable(tableSchema);

            System.out.println("Table with relationships '" + schemaName + "' created successfully in MongoDB for project " + projectUuid);

        } catch (Exception e) {
            System.err.println("Error creating table with relationships: " + e.getMessage());
            throw new RuntimeException("Failed to create table with relationships: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TableSchema> getRelatedTables(String projectId, String schemaName) {
        try {
            System.out.println("DEBUG: Looking for tables related to '" + schemaName + "' in project: " + projectId);

            List<TableSchema> projectTables = getTablesByProjectId(projectId);
            System.out.println("DEBUG: Found " + projectTables.size() + " tables in project");

            // Debug: Print all tables and their relationships
            for (TableSchema table : projectTables) {
                System.out.println("DEBUG: Table '" + table.getSchemaName() + "' has relationships: " +
                    (table.getRelationships() != null ? table.getRelationships().size() : 0));

                if (table.getRelationships() != null) {
                    for (TableSchema.TableRelationship rel : table.getRelationships()) {
                        System.out.println("DEBUG: - Relationship: " + rel.getForeignKeyColumn() + " -> " +
                            rel.getReferencedTable() + "." + rel.getReferencedColumn());
                    }
                }
            }

            List<TableSchema> relatedTables = projectTables.stream()
                    .filter(table -> {
                        if (table.getRelationships() == null || table.getRelationships().isEmpty()) {
                            return false;
                        }
                        return table.getRelationships().stream()
                                .anyMatch(rel -> rel.getReferencedTable().equals(schemaName));
                    })
                    .toList();

            System.out.println("DEBUG: Found " + relatedTables.size() + " tables that reference '" + schemaName + "'");

            return relatedTables;
        } catch (Exception e) {
            System.err.println("Error getting related tables: " + e.getMessage());
            throw new RuntimeException("Failed to get related tables: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRecordWithRelations(String schemaName, String id, String projectId) {
        try {
            // First, verify the table exists in the project
            TableSchema tableSchema = getTableByNameAndProject(schemaName, projectId);
            if (tableSchema == null) {
                throw new RuntimeException("Table '" + schemaName + "' does not exist in project " + projectId);
            }

            // Get the main record by searching in the specific table and project
            Optional<TableData> tableDataOpt = tableDataRepository.findByIdAndSchemaNameAndProjectId(
                id, schemaName, projectId
            );
            if (tableDataOpt.isEmpty()) {
                return null;
            }

            TableData tableData = tableDataOpt.get();
            Map<String, Object> record = new HashMap<>(tableData.getData());
            record.put("id", tableData.getId());
            record.put("createdAt", tableData.getCreatedAt());
            record.put("updatedAt", tableData.getUpdatedAt());

            // Check if relationships are defined
            if (tableSchema.getRelationships() == null || tableSchema.getRelationships().isEmpty()) {
                System.out.println("DEBUG: No relationships defined for table '" + schemaName + "'");
                return record; // No relationships defined, return basic record
            }

            System.out.println("DEBUG: Processing " + tableSchema.getRelationships().size() + " relationships for table '" + schemaName + "'");

            // Load related data for each foreign key
            for (TableSchema.TableRelationship rel : tableSchema.getRelationships()) {
                Object foreignKeyValue = record.get(rel.getForeignKeyColumn());
                System.out.println("DEBUG: Processing relationship - " + rel.getForeignKeyColumn() + " -> " +
                    rel.getReferencedTable() + "." + rel.getReferencedColumn() + ", value: " + foreignKeyValue);

                if (foreignKeyValue != null) {
                    try {
                        // Find related record in the same project
                        List<TableData> relatedRecords = tableDataRepository.findBySchemaNameAndProjectId(
                            rel.getReferencedTable(), projectId);

                        System.out.println("DEBUG: Found " + relatedRecords.size() + " records in table '" + rel.getReferencedTable() + "'");

                        Map<String, Object> relatedRecord = null;

                        // Handle different types of foreign key matching
                        if ("id".equals(rel.getReferencedColumn())) {
                            // Matching with MongoDB ObjectId
                            relatedRecord = relatedRecords.stream()
                                .filter(data -> data.getId().equals(foreignKeyValue.toString()))
                                .findFirst()
                                .map(data -> {
                                    Map<String, Object> relData = new HashMap<>(data.getData());
                                    relData.put("id", data.getId());
                                    relData.put("createdAt", data.getCreatedAt());
                                    relData.put("updatedAt", data.getUpdatedAt());
                                    return relData;
                                })
                                .orElse(null);
                        } else {
                            // Matching with a specific column in the referenced table's data
                            relatedRecord = relatedRecords.stream()
                                .filter(data -> {
                                    Object referencedValue = data.getData().get(rel.getReferencedColumn());
                                    if (referencedValue == null) return false;
                                    return referencedValue.toString().equals(foreignKeyValue.toString());
                                })
                                .findFirst()
                                .map(data -> {
                                    Map<String, Object> relData = new HashMap<>(data.getData());
                                    relData.put("id", data.getId());
                                    relData.put("createdAt", data.getCreatedAt());
                                    relData.put("updatedAt", data.getUpdatedAt());
                                    return relData;
                                })
                                .orElse(null);
                        }

                        if (relatedRecord != null) {
                            System.out.println("DEBUG: Found related record for " + rel.getForeignKeyColumn());
                        } else {
                            System.out.println("DEBUG: No related record found for " + rel.getForeignKeyColumn() + " with value " + foreignKeyValue);
                        }

                        record.put(rel.getForeignKeyColumn() + "_data", relatedRecord);

                    } catch (Exception e) {
                        // If related record doesn't exist or table doesn't exist, add null
                        record.put(rel.getForeignKeyColumn() + "_data", null);
                        System.out.println("Warning: Could not load related data for " + rel.getReferencedTable() + " with id " + foreignKeyValue + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("DEBUG: Foreign key '" + rel.getForeignKeyColumn() + "' is null, skipping relationship");
                }
            }

            return record;

        } catch (Exception e) {
            System.err.println("Error getting record with relations: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get record with relations: " + e.getMessage(), e);
        }
    }
}
