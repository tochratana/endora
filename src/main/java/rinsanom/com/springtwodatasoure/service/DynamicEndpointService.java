package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.List;
import java.util.Map;

public interface DynamicEndpointService {
    void generateEndpointsForTable(TableSchema tableSchema);
    void removeEndpointsForTable(String tableName);
    String getEndpointDocumentation(String tableName);
    Map<String, String> getAllEndpointDocumentation();

    // New methods for MongoDB functionality
    EndpointDocumentation getEndpointDocumentationEntity(String tableName);
    EndpointDocumentation getEndpointDocumentationByTableAndProject(String tableName, String projectId);
    List<EndpointDocumentation> getEndpointDocumentationByProject(String projectId);
    void removeEndpointsForTableAndProject(String tableName, String projectId);
}
