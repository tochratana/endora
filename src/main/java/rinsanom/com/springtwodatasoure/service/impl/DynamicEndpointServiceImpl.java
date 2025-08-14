package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DynamicEndpointServiceImpl implements DynamicEndpointService {

    // Store generated endpoint information
    private final Map<String, String> generatedEndpoints = new ConcurrentHashMap<>();

    @Override
    public void generateEndpointsForTable(TableSchema tableSchema) {
        String tableName = tableSchema.getTableName();
        String basePath = "/api/tables/" + tableName;

        // Store endpoint documentation
        String documentation = generateDocumentation(tableName, tableSchema);
        generatedEndpoints.put(tableName, documentation);

        System.out.println("=== AUTO-GENERATED ENDPOINTS FOR TABLE: " + tableName.toUpperCase() + " ===");
        System.out.println(documentation);
        System.out.println("=== END OF GENERATED ENDPOINTS ===\n");
    }

    @Override
    public void removeEndpointsForTable(String tableName) {
        generatedEndpoints.remove(tableName);
        System.out.println("Removed endpoints documentation for table: " + tableName);
    }

    @Override
    public String getEndpointDocumentation(String tableName) {
        return generatedEndpoints.getOrDefault(tableName, "No documentation found for table: " + tableName);
    }

    public Map<String, String> getAllEndpointDocumentation() {
        return new ConcurrentHashMap<>(generatedEndpoints);
    }

    private String generateDocumentation(String tableName, TableSchema tableSchema) {
        String basePath = "/api/tables/" + tableName;
        StringBuilder doc = new StringBuilder();

        doc.append("Table: ").append(tableName).append("\n");
        doc.append("Project ID: ").append(tableSchema.getProjectId()).append("\n");
        doc.append("Schema: ").append(tableSchema.getSchema()).append("\n\n");

        doc.append("Available Endpoints:\n");
        doc.append("==================\n\n");

        // GET all records
        doc.append("1. GET ").append(basePath).append("\n");
        doc.append("   Description: Get all records from ").append(tableName).append(" table\n");
        doc.append("   Response: Array of objects\n");
        doc.append("   Example: curl -X GET http://localhost:8080").append(basePath).append("\n\n");

        // POST create record
        doc.append("2. POST ").append(basePath).append("\n");
        doc.append("   Description: Create a new record in ").append(tableName).append(" table\n");
        doc.append("   Request Body: JSON object with table columns\n");
        doc.append("   Example Schema: ").append(generateExampleJson(tableSchema.getSchema())).append("\n");
        doc.append("   Example: curl -X POST http://localhost:8080").append(basePath)
           .append(" -H \"Content-Type: application/json\" -d '").append(generateExampleJson(tableSchema.getSchema())).append("'\n\n");

        // GET by ID
        doc.append("3. GET ").append(basePath).append("/{id}\n");
        doc.append("   Description: Get a specific record by ID\n");
        doc.append("   Parameters: id (path parameter)\n");
        doc.append("   Example: curl -X GET http://localhost:8080").append(basePath).append("/1\n\n");

        // PUT update
        doc.append("4. PUT ").append(basePath).append("/{id}\n");
        doc.append("   Description: Update a specific record by ID\n");
        doc.append("   Parameters: id (path parameter)\n");
        doc.append("   Request Body: JSON object with updated values\n");
        doc.append("   Example: curl -X PUT http://localhost:8080").append(basePath)
           .append("/1 -H \"Content-Type: application/json\" -d '{\"name\":\"Updated Name\"}'\n\n");

        // DELETE
        doc.append("5. DELETE ").append(basePath).append("/{id}\n");
        doc.append("   Description: Delete a specific record by ID\n");
        doc.append("   Parameters: id (path parameter)\n");
        doc.append("   Example: curl -X DELETE http://localhost:8080").append(basePath).append("/1\n\n");

        return doc.toString();
    }

    private String generateExampleJson(Map<String, String> schema) {
        StringBuilder json = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            if (count > 0) json.append(", ");
            json.append("\"").append(entry.getKey()).append("\": ");

            // Generate example values based on data type
            String dataType = entry.getValue().toUpperCase();
            if (dataType.contains("VARCHAR") || dataType.contains("TEXT")) {
                json.append("\"example_").append(entry.getKey()).append("\"");
            } else if (dataType.contains("INT") || dataType.contains("BIGINT")) {
                json.append("123");
            } else if (dataType.contains("DECIMAL") || dataType.contains("FLOAT")) {
                json.append("123.45");
            } else if (dataType.contains("BOOLEAN")) {
                json.append("true");
            } else if (dataType.contains("DATE")) {
                json.append("\"2023-12-01\"");
            } else {
                json.append("\"example_value\"");
            }
            count++;
        }
        json.append("}");
        return json.toString();
    }
}
