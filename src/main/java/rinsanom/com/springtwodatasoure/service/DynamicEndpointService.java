package rinsanom.com.springtwodatasoure.service;

import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.Map;

public interface DynamicEndpointService {
    void generateEndpointsForTable(TableSchema tableSchema);
    void removeEndpointsForTable(String tableName);
    String getEndpointDocumentation(String tableName);
    Map<String, String> getAllEndpointDocumentation();
}
