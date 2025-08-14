package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.dto.CreateTableWithRelationshipsDTO;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.List;
import java.util.Map;

public interface TableService {
    void createTables(String projectId , String tableName, Map< String , String> schema  );

    void dropTables();

    void createMongoCollection();

    void dropMongoCollection();

    List<TableSchema> getAllTables();

    List<TableSchema> getTablesByProjectId(String projectId);

    TableSchema getTableByNameAndProject(String tableName, String projectId);

    // New methods for data operations
    void insertData(String tableName, String projectId,    Map<String, Object> data);

    List<Map<String, Object>> getAllDataFromTable(String tableName);

    List<Map<String, Object>> getDataFromTableByProject(String tableName, String projectId);

    // Additional CRUD methods for dynamic endpoints
    Map<String, Object> getRecordById(String tableName, String id);

    void updateRecord(String tableName, String id, Map<String, Object> data);

    void deleteRecord(String tableName, String id);

    // New methods for handling table relationships
    void createTableWithRelationships(String projectId, String tableName, Map<String, String> schema, List<CreateTableWithRelationshipsDTO.TableRelationship> relationships);

    List<TableSchema> getRelatedTables(String projectId, String tableName);

    Map<String, Object> getRecordWithRelations(String tableName, String id, String projectId);
}
