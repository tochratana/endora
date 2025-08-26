package rinsanom.com.springtwodatasoure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DynamicOpenApiService {

    private final TableService tableService;

    public Map<String, Object> generateOpenApiForProject(String projectId) {
        // Get all tables for the project
        List<TableSchema> tables = tableService.getTablesByProjectId(projectId);

        // Create OpenAPI spec as a raw Map to avoid serialization issues
        Map<String, Object> openApiSpec = new HashMap<>();

        openApiSpec.put("openapi", "3.0.3");

        // Info
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Project " + projectId + " - Generated APIs");
        info.put("version", "1.0.0");
        info.put("description", "Auto-generated REST APIs for your custom tables");
        openApiSpec.put("info", info);

        // Servers
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = new HashMap<>();
        server.put("url", "http://localhost:8080");
        server.put("description", "Development server");
        servers.add(server);
        openApiSpec.put("servers", servers);

        // Paths
        Map<String, Object> paths = new HashMap<>();

        // Generate paths for each table
        for (TableSchema table : tables) {
            String tableName = table.getSchemaName();
            generateTablePaths(paths, tableName, table);
        }

        openApiSpec.put("paths", paths);
        return openApiSpec;
    }

    private void generateTablePaths(Map<String, Object> paths, String tableName, TableSchema tableSchema) {
        String basePath = "/api/tables/" + tableName;

        // Base path operations (GET all, POST)
        Map<String, Object> basePathOperations = new HashMap<>();

        // GET all records
        Map<String, Object> getAllOperation = new HashMap<>();
        getAllOperation.put("summary", "Get all " + tableName);
        getAllOperation.put("description", "Retrieve all records from " + tableName + " table");
        getAllOperation.put("tags", Arrays.asList(tableName));
        getAllOperation.put("responses", createGetAllResponses(tableName, tableSchema));
        basePathOperations.put("get", getAllOperation);

        // POST new record
        Map<String, Object> postOperation = new HashMap<>();
        postOperation.put("summary", "Create " + tableName);
        postOperation.put("description", "Create a new record in " + tableName + " table");
        postOperation.put("tags", Arrays.asList(tableName));
        postOperation.put("requestBody", createRequestBody(tableName, tableSchema));
        postOperation.put("responses", createPostResponses(tableName, tableSchema));
        basePathOperations.put("post", postOperation);

        paths.put(basePath, basePathOperations);

        // ID-based path operations (GET by ID, PUT, DELETE)
        String idPath = basePath + "/{id}";
        Map<String, Object> idPathOperations = new HashMap<>();

        // GET by ID
        Map<String, Object> getByIdOperation = new HashMap<>();
        getByIdOperation.put("summary", "Get " + tableName + " by ID");
        getByIdOperation.put("description", "Retrieve a specific record from " + tableName + " table");
        getByIdOperation.put("tags", Arrays.asList(tableName));
        getByIdOperation.put("parameters", Arrays.asList(createIdParameter()));
        getByIdOperation.put("responses", createGetByIdResponses(tableName, tableSchema));
        idPathOperations.put("get", getByIdOperation);

        // PUT by ID
        Map<String, Object> putOperation = new HashMap<>();
        putOperation.put("summary", "Update " + tableName + " by ID");
        putOperation.put("description", "Update a specific record in " + tableName + " table");
        putOperation.put("tags", Arrays.asList(tableName));
        putOperation.put("parameters", Arrays.asList(createIdParameter()));
        putOperation.put("requestBody", createRequestBody(tableName, tableSchema));
        putOperation.put("responses", createPutResponses(tableName, tableSchema));
        idPathOperations.put("put", putOperation);

        // DELETE by ID
        Map<String, Object> deleteOperation = new HashMap<>();
        deleteOperation.put("summary", "Delete " + tableName + " by ID");
        deleteOperation.put("description", "Delete a specific record from " + tableName + " table");
        deleteOperation.put("tags", Arrays.asList(tableName));
        deleteOperation.put("parameters", Arrays.asList(createIdParameter()));
        deleteOperation.put("responses", createDeleteResponses());
        idPathOperations.put("delete", deleteOperation);

        paths.put(idPath, idPathOperations);
    }

    private Map<String, Object> createIdParameter() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("name", "id");
        parameter.put("in", "path");
        parameter.put("description", "Record ID");
        parameter.put("required", true);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        parameter.put("schema", schema);

        return parameter;
    }

    private Map<String, Object> createRequestBody(String tableName, TableSchema tableSchema) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("description", "Request body for " + tableName);
        requestBody.put("required", true);

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        mediaType.put("schema", createTableSchema(tableName, tableSchema));
        content.put("application/json", mediaType);
        requestBody.put("content", content);

        return requestBody;
    }

    private Map<String, Object> createTableSchema(String tableName, TableSchema tableSchema) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("title", tableName + " Schema");

        Map<String, Object> properties = new HashMap<>();

        // Add projectId as a required field
        Map<String, Object> projectIdProperty = new HashMap<>();
        projectIdProperty.put("type", "string");
        projectIdProperty.put("description", "Project ID for this record");
        properties.put("projectId", projectIdProperty);

        if (tableSchema.getSchema() != null) {
            tableSchema.getSchema().forEach((columnName, columnType) -> {
                Map<String, Object> propertySchema = new HashMap<>();
                propertySchema.put("type", convertToOpenApiType(columnType));
                propertySchema.put("description", "Field: " + columnName + " (Type: " + columnType + ")");

                // Add format for specific types
                String openApiType = convertToOpenApiType(columnType);
                if ("integer".equals(openApiType)) {
                    propertySchema.put("format", "int32");
                } else if ("number".equals(openApiType)) {
                    propertySchema.put("format", "double");
                }

                properties.put(columnName, propertySchema);
            });
        }

        schema.put("properties", properties);

        // Mark projectId as required
        schema.put("required", Arrays.asList("projectId"));

        return schema;
    }

    private Map<String, Object> createGetAllResponses(String tableName, TableSchema tableSchema) {
        Map<String, Object> responses = new HashMap<>();

        // 200 response
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "List of " + tableName + " records");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();

        Map<String, Object> arraySchema = new HashMap<>();
        arraySchema.put("type", "array");
        arraySchema.put("items", createTableSchema(tableName, tableSchema));

        mediaType.put("schema", arraySchema);
        content.put("application/json", mediaType);
        successResponse.put("content", content);

        responses.put("200", successResponse);
        responses.put("400", createErrorResponse("Bad Request"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createPostResponses(String tableName, TableSchema tableSchema) {
        Map<String, Object> responses = new HashMap<>();

        // 201 response
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Created " + tableName + " record");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        mediaType.put("schema", createTableSchema(tableName, tableSchema));
        content.put("application/json", mediaType);
        successResponse.put("content", content);

        responses.put("201", successResponse);
        responses.put("400", createErrorResponse("Bad Request"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createGetByIdResponses(String tableName, TableSchema tableSchema) {
        Map<String, Object> responses = new HashMap<>();

        // 200 response
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", tableName + " record");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        mediaType.put("schema", createTableSchema(tableName, tableSchema));
        content.put("application/json", mediaType);
        successResponse.put("content", content);

        responses.put("200", successResponse);
        responses.put("404", createErrorResponse("Record not found"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createPutResponses(String tableName, TableSchema tableSchema) {
        Map<String, Object> responses = new HashMap<>();

        // 200 response
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Updated " + tableName + " record");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        mediaType.put("schema", createTableSchema(tableName, tableSchema));
        content.put("application/json", mediaType);
        successResponse.put("content", content);

        responses.put("200", successResponse);
        responses.put("404", createErrorResponse("Record not found"));
        responses.put("400", createErrorResponse("Bad Request"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createDeleteResponses() {
        Map<String, Object> responses = new HashMap<>();

        // 200 response
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Record deleted successfully");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();

        Map<String, Object> messageSchema = new HashMap<>();
        messageSchema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> messageProperty = new HashMap<>();
        messageProperty.put("type", "string");
        properties.put("message", messageProperty);
        messageSchema.put("properties", properties);

        mediaType.put("schema", messageSchema);
        content.put("application/json", mediaType);
        successResponse.put("content", content);

        responses.put("200", successResponse);
        responses.put("404", createErrorResponse("Record not found"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createErrorResponse(String description) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("description", description);
        return errorResponse;
    }

    private String convertToOpenApiType(String columnType) {
        if (columnType == null) return "string";

        switch (columnType.toLowerCase()) {
            case "int":
            case "integer":
                return "integer";
            case "double":
            case "float":
            case "decimal":
                return "number";
            case "boolean":
            case "bool":
                return "boolean";
            default:
                return "string";
        }
    }
}