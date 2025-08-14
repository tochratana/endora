package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.dto.CreateTableWithRelationshipsDTO;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.entity.TableData;
import rinsanom.com.springtwodatasoure.repository.mongo.TableSchemaRepository;
import rinsanom.com.springtwodatasoure.repository.mongo.TableDataRepository;
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
    private final DynamicEndpointService dynamicEndpointService;

    @Override
    public void createTables(String projectId, String tableName, Map<String, String> schema) {
        try {
            // Check if table schema already exists in MongoDB
            Optional<TableSchema> existingTable = tableSchemaRepository.findByTableNameAndProjectId(tableName, projectId);

            if (existingTable.isPresent()) {
                throw new RuntimeException("Table '" + tableName + "' already exists in project " + projectId);
            }

            // Store table schema in MongoDB (no PostgreSQL table creation)
            TableSchema tableSchema = new TableSchema(tableName, projectId, schema);
            tableSchemaRepository.save(tableSchema);

            // AUTO-GENERATE ENDPOINTS FOR THE NEW TABLE
            dynamicEndpointService.generateEndpointsForTable(tableSchema);

            System.out.println("Table schema '" + tableName + "' created successfully in MongoDB for project " + projectId);

        } catch (Exception e) {
            System.err.println("Error creating table schema: " + e.getMessage());
            throw new RuntimeException("Failed to create table schema: " + e.getMessage(), e);
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
    public List<TableSchema> getTablesByProjectId(String projectId) {
        return tableSchemaRepository.findByProjectId(projectId);
    }

    @Override
    public TableSchema getTableByNameAndProject(String tableName, String projectId) {
        return tableSchemaRepository.findByTableNameAndProjectId(tableName, projectId)
                .orElse(null);
    }

    @Override
    public void insertData(String tableName, String projectId ,  Map<String, Object> data) {
        try {
            // Get the project ID from the data or require it as a parameter
            log.info("Attempting to insert data into table: {}, projectId: {}", tableName, projectId);
            if (projectId == null) {
                throw new RuntimeException("Project ID is required when inserting data");
            }

            // Verify table schema exists
            TableSchema tableSchema = getTableByNameAndProject(tableName, projectId);
            if (tableSchema == null) {
                throw new RuntimeException("Table '" + tableName + "' does not exist in project " + projectId);
            }

            // Create new table data document in MongoDB
            TableData tableData = new TableData(tableName, projectId, data);
            tableDataRepository.save(tableData);

            System.out.println("Data inserted successfully into table '" + tableName + "' for project " + projectId);

        } catch (Exception e) {
            System.err.println("Error inserting data: " + e.getMessage());
            throw new RuntimeException("Failed to insert data: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllDataFromTable(String tableName) {
        try {
            List<TableData> tableDataList = tableDataRepository.findByTableName(tableName);
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
    public List<Map<String, Object>> getDataFromTableByProject(String tableName, String projectId) {
        try {
            List<TableData> tableDataList = tableDataRepository.findByTableNameAndProjectId(tableName, projectId);
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
    public Map<String, Object> getRecordById(String tableName, String id) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getTableName().equals(tableName)) {
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
    public void updateRecord(String tableName, String id, Map<String, Object> data) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getTableName().equals(tableName)) {
                throw new RuntimeException("No record found with ID: " + id + " in table: " + tableName);
            }

            TableData tableData = tableDataOpt.get();

            // Update the data fields
            Map<String, Object> updatedData = new HashMap<>(tableData.getData());
            updatedData.putAll(data);

            tableData.setData(updatedData);
            tableData.updateTimestamp();

            tableDataRepository.save(tableData);

            System.out.println("Record updated successfully in table '" + tableName + "' with ID: " + id);

        } catch (Exception e) {
            System.err.println("Error updating record: " + e.getMessage());
            throw new RuntimeException("Failed to update record: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRecord(String tableName, String id) {
        try {
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() || !tableDataOpt.get().getTableName().equals(tableName)) {
                throw new RuntimeException("No record found with ID: " + id + " in table: " + tableName);
            }

            tableDataRepository.deleteById(id);

            System.out.println("Record deleted successfully from table '" + tableName + "' with ID: " + id);

        } catch (Exception e) {
            System.err.println("Error deleting record: " + e.getMessage());
            throw new RuntimeException("Failed to delete record: " + e.getMessage(), e);
        }
    }

    @Override
    public void createTableWithRelationships(String projectId, String tableName, Map<String, String> schema,
                                           List<CreateTableWithRelationshipsDTO.TableRelationship> relationships) {
        try {
            // Check if table schema already exists in MongoDB
            Optional<TableSchema> existingTable = tableSchemaRepository.findByTableNameAndProjectId(tableName, projectId);

            if (existingTable.isPresent()) {
                throw new RuntimeException("Table '" + tableName + "' already exists in project " + projectId);
            }

            // Validate that referenced tables exist in the same project
            if (relationships != null) {
                for (CreateTableWithRelationshipsDTO.TableRelationship rel : relationships) {
                    TableSchema referencedTable = getTableByNameAndProject(rel.getReferencedTable(), projectId);
                    if (referencedTable == null) {
                        throw new RuntimeException("Referenced table '" + rel.getReferencedTable() + "' does not exist in project " + projectId);
                    }
                }
            }

            // Create table schema with relationships (no PostgreSQL table creation)
            TableSchema tableSchema = new TableSchema(tableName, projectId, schema);

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

            // Store table schema with relationships in MongoDB
            tableSchemaRepository.save(tableSchema);

            // AUTO-GENERATE ENDPOINTS FOR THE NEW TABLE
            dynamicEndpointService.generateEndpointsForTable(tableSchema);

            System.out.println("Table schema with relationships '" + tableName + "' created successfully in MongoDB for project " + projectId);

        } catch (Exception e) {
            System.err.println("Error creating table schema with relationships: " + e.getMessage());
            throw new RuntimeException("Failed to create table schema with relationships: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TableSchema> getRelatedTables(String projectId, String tableName) {
        try {
            List<TableSchema> projectTables = getTablesByProjectId(projectId);
            return projectTables.stream()
                    .filter(table -> {
                        if (table.getRelationships() == null) return false;
                        return table.getRelationships().stream()
                                .anyMatch(rel -> rel.getReferencedTable().equals(tableName));
                    })
                    .toList();
        } catch (Exception e) {
            System.err.println("Error getting related tables: " + e.getMessage());
            throw new RuntimeException("Failed to get related tables: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRecordWithRelations(String tableName, String id, String projectId) {
        try {
            // First, verify the table exists in the project
            TableSchema tableSchema = getTableByNameAndProject(tableName, projectId);
            if (tableSchema == null) {
                throw new RuntimeException("Table '" + tableName + "' does not exist in project " + projectId);
            }

            // Get the main record by searching in the specific table and project
            Optional<TableData> tableDataOpt = tableDataRepository.findById(id);
            if (tableDataOpt.isEmpty() ||
                !tableDataOpt.get().getTableName().equals(tableName) ||
                !tableDataOpt.get().getProjectId().equals(projectId)) {
                return null;
            }

            TableData tableData = tableDataOpt.get();
            Map<String, Object> record = new HashMap<>(tableData.getData());
            record.put("id", tableData.getId());
            record.put("createdAt", tableData.getCreatedAt());
            record.put("updatedAt", tableData.getUpdatedAt());

            // Check if relationships are defined
            if (tableSchema.getRelationships() == null || tableSchema.getRelationships().isEmpty()) {
                return record; // No relationships defined, return basic record
            }

            // Load related data for each foreign key
            for (TableSchema.TableRelationship rel : tableSchema.getRelationships()) {
                Object foreignKeyValue = record.get(rel.getForeignKeyColumn());
                if (foreignKeyValue != null) {
                    try {
                        // Find related record in the same project
                        List<TableData> relatedRecords = tableDataRepository.findByTableNameAndProjectId(
                            rel.getReferencedTable(), projectId);

                        Map<String, Object> relatedRecord = relatedRecords.stream()
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

                        record.put(rel.getForeignKeyColumn() + "_data", relatedRecord);
                    } catch (Exception e) {
                        // If related record doesn't exist or table doesn't exist, add null
                        record.put(rel.getForeignKeyColumn() + "_data", null);
                        System.out.println("Warning: Could not load related data for " + rel.getReferencedTable() + " with id " + foreignKeyValue);
                    }
                }
            }

            return record;

        } catch (Exception e) {
            System.err.println("Error getting record with relations: " + e.getMessage());
            throw new RuntimeException("Failed to get record with relations: " + e.getMessage(), e);
        }
    }
}
