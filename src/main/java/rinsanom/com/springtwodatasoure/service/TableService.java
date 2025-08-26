package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.CreateTableWithRelationshipsDTO;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.List;
import java.util.Map;

public interface TableService {
    void createTables(String projectId , String schemaName, Map< String , String> schema  );

    void dropTables();

    void createMongoCollection();

    void dropMongoCollection();

    List<TableSchema> getAllTables();

    List<TableSchema> getTablesByProjectId(String projectId);

    TableSchema getTableByNameAndProject(String schemaName, String projectId);

    // New methods for data operations
    void insertData(String schemaName, String projectId, Map<String, Object> data);

    List<Map<String, Object>> getAllDataFromTable(String schemaName);

    List<Map<String, Object>> getDataFromTableByProject(String schemaName, String projectId);

    // Additional CRUD methods for dynamic endpoints
    Map<String, Object> getRecordById(String schemaName, String id);

    void updateRecord(String schemaName, String id, Map<String, Object> data);

    void deleteRecord(String schemaName, String id);

    // New methods for handling table relationships
    void createTableWithRelationships(String projectId, String tableName, Map<String, String> schema, List<CreateTableWithRelationshipsDTO.TableRelationship> relationships);

    List<TableSchema> getRelatedTables(String projectId, String tableName);

    Map<String, Object> getRecordWithRelations(String tableName, String id, String projectId);
}
