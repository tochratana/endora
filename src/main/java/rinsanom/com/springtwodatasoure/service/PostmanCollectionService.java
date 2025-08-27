package rinsanom.com.springtwodatasoure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostmanCollectionService {

    private final TableService tableService;

    public Map<String, Object> generatePostmanCollectionForProject(String projectId) {
        // Get all tables for the project
        List<TableSchema> tables = tableService.getTablesByProjectId(projectId);

        // Create Postman collection structure
        Map<String, Object> collection = new HashMap<>();

        // Collection info
        Map<String, Object> info = new HashMap<>();
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put("name", "Project " + projectId + " - Generated APIs");
        info.put("description", "Auto-generated REST APIs for your custom tables");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        // Variables
        List<Map<String, Object>> variables = new ArrayList<>();
        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("key", "baseUrl");
        baseUrlVar.put("value", "http://localhost:8080");
        baseUrlVar.put("type", "string");
        variables.add(baseUrlVar);
        collection.put("variable", variables);

        // Items (API endpoints)
        List<Map<String, Object>> items = new ArrayList<>();

        for (TableSchema table : tables) {
            String tableName = table.getSchemaName();

            // Create folder for each table
            Map<String, Object> tableFolder = new HashMap<>();
            tableFolder.put("name", tableName + " APIs");
            tableFolder.put("description", "CRUD operations for " + tableName + " table");

            List<Map<String, Object>> tableItems = new ArrayList<>();

            // GET all records
            tableItems.add(createGetAllRequest(tableName));

            // POST new record
            tableItems.add(createPostRequest(tableName, table));

            // GET by ID
            tableItems.add(createGetByIdRequest(tableName));

            // PUT by ID
            tableItems.add(createPutRequest(tableName, table));

            // DELETE by ID
            tableItems.add(createDeleteRequest(tableName));

            tableFolder.put("item", tableItems);
            items.add(tableFolder);
        }

        collection.put("item", items);

        return collection;
    }

    private Map<String, Object> createGetAllRequest(String tableName) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Get all " + tableName);
        request.put("description", "Retrieve all records from " + tableName + " table with pagination and sorting");

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");

        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{baseUrl}}/api/tables/" + tableName + "?page=0&size=20");
        url.put("host", Arrays.asList("{{baseUrl}}"));
        url.put("path", Arrays.asList("api", "tables", tableName));

        List<Map<String, Object>> query = new ArrayList<>();
        query.add(createQueryParam("page", "0", "Page number (starts from 0)"));
        query.add(createQueryParam("size", "20", "Number of records per page"));
        query.add(createQueryParam("sort", "", "Sort criteria (e.g., id,desc)"));
        url.put("query", query);

        requestDetails.put("url", url);
        request.put("request", requestDetails);

        // Add example responses
        List<Map<String, Object>> responses = new ArrayList<>();
        responses.add(createSuccessResponse("200", "List of " + tableName + " records"));
        request.put("response", responses);

        return request;
    }

    private Map<String, Object> createPostRequest(String tableName, TableSchema tableSchema) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Create " + tableName);
        request.put("description", "Create a new record in " + tableName + " table");

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "POST");

        // Headers
        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> contentTypeHeader = new HashMap<>();
        contentTypeHeader.put("key", "Content-Type");
        contentTypeHeader.put("value", "application/json");
        headers.add(contentTypeHeader);
        requestDetails.put("header", headers);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");
        body.put("raw", createSampleRequestBody(tableName, tableSchema));
        requestDetails.put("body", body);

        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{baseUrl}}/api/tables/" + tableName);
        url.put("host", Arrays.asList("{{baseUrl}}"));
        url.put("path", Arrays.asList("api", "tables", tableName));
        requestDetails.put("url", url);

        request.put("request", requestDetails);

        // Add example responses
        List<Map<String, Object>> responses = new ArrayList<>();
        responses.add(createSuccessResponse("201", "Created " + tableName + " record"));
        request.put("response", responses);

        return request;
    }

    private Map<String, Object> createGetByIdRequest(String tableName) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Get " + tableName + " by ID");
        request.put("description", "Retrieve a specific record from " + tableName + " table");

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");

        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{baseUrl}}/api/tables/" + tableName + "/{{recordId}}");
        url.put("host", Arrays.asList("{{baseUrl}}"));
        url.put("path", Arrays.asList("api", "tables", tableName, "{{recordId}}"));

        List<Map<String, Object>> variables = new ArrayList<>();
        Map<String, Object> idVar = new HashMap<>();
        idVar.put("key", "recordId");
        idVar.put("value", "1");
        idVar.put("description", "ID of the record to retrieve");
        variables.add(idVar);
        url.put("variable", variables);

        requestDetails.put("url", url);
        request.put("request", requestDetails);

        // Add example responses
        List<Map<String, Object>> responses = new ArrayList<>();
        responses.add(createSuccessResponse("200", tableName + " record"));
        responses.add(createErrorResponse("404", "Record not found"));
        request.put("response", responses);

        return request;
    }

    private Map<String, Object> createPutRequest(String tableName, TableSchema tableSchema) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Update " + tableName + " by ID");
        request.put("description", "Update a specific record in " + tableName + " table");

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "PUT");

        // Headers
        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> contentTypeHeader = new HashMap<>();
        contentTypeHeader.put("key", "Content-Type");
        contentTypeHeader.put("value", "application/json");
        headers.add(contentTypeHeader);
        requestDetails.put("header", headers);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");
        body.put("raw", createSampleRequestBody(tableName, tableSchema));
        requestDetails.put("body", body);

        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{baseUrl}}/api/tables/" + tableName + "/{{recordId}}");
        url.put("host", Arrays.asList("{{baseUrl}}"));
        url.put("path", Arrays.asList("api", "tables", tableName, "{{recordId}}"));

        List<Map<String, Object>> variables = new ArrayList<>();
        Map<String, Object> idVar = new HashMap<>();
        idVar.put("key", "recordId");
        idVar.put("value", "1");
        idVar.put("description", "ID of the record to update");
        variables.add(idVar);
        url.put("variable", variables);

        requestDetails.put("url", url);
        request.put("request", requestDetails);

        // Add example responses
        List<Map<String, Object>> responses = new ArrayList<>();
        responses.add(createSuccessResponse("200", "Updated " + tableName + " record"));
        responses.add(createErrorResponse("404", "Record not found"));
        request.put("response", responses);

        return request;
    }

    private Map<String, Object> createDeleteRequest(String tableName) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Delete " + tableName + " by ID");
        request.put("description", "Delete a specific record from " + tableName + " table");

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "DELETE");

        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{baseUrl}}/api/tables/" + tableName + "/{{recordId}}");
        url.put("host", Arrays.asList("{{baseUrl}}"));
        url.put("path", Arrays.asList("api", "tables", tableName, "{{recordId}}"));

        List<Map<String, Object>> variables = new ArrayList<>();
        Map<String, Object> idVar = new HashMap<>();
        idVar.put("key", "recordId");
        idVar.put("value", "1");
        idVar.put("description", "ID of the record to delete");
        variables.add(idVar);
        url.put("variable", variables);

        requestDetails.put("url", url);
        request.put("request", requestDetails);

        // Add example responses
        List<Map<String, Object>> responses = new ArrayList<>();
        responses.add(createSuccessResponse("200", "Record deleted successfully"));
        responses.add(createErrorResponse("404", "Record not found"));
        request.put("response", responses);

        return request;
    }

    private Map<String, Object> createQueryParam(String key, String value, String description) {
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        param.put("value", value);
        param.put("description", description);
        param.put("disabled", value.isEmpty());
        return param;
    }

    private Map<String, Object> createSuccessResponse(String code, String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("code", Integer.parseInt(code));
        response.put("status", getStatusText(code));

        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> contentTypeHeader = new HashMap<>();
        contentTypeHeader.put("key", "Content-Type");
        contentTypeHeader.put("value", "application/json");
        headers.add(contentTypeHeader);
        response.put("header", headers);

        return response;
    }

    private Map<String, Object> createErrorResponse(String code, String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("code", Integer.parseInt(code));
        response.put("status", getStatusText(code));

        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> contentTypeHeader = new HashMap<>();
        contentTypeHeader.put("key", "Content-Type");
        contentTypeHeader.put("value", "application/json");
        headers.add(contentTypeHeader);
        response.put("header", headers);

        String errorBody = "{\n  \"message\": \"" + name + "\"\n}";
        response.put("body", errorBody);

        return response;
    }

    private String getStatusText(String code) {
        switch (code) {
            case "200": return "OK";
            case "201": return "Created";
            case "400": return "Bad Request";
            case "404": return "Not Found";
            case "500": return "Internal Server Error";
            default: return "Unknown";
        }
    }

    private String createSampleRequestBody(String tableName, TableSchema tableSchema) {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"projectId\": \"proj_12345\"");

        if (tableSchema.getSchema() != null) {
            for (Map.Entry<String, String> entry : tableSchema.getSchema().entrySet()) {
                json.append(",\n  \"").append(entry.getKey()).append("\": ");
                json.append(getSampleValueForType(entry.getValue()));
            }
        }

        json.append("\n}");
        return json.toString();
    }

    private String getSampleValueForType(String columnType) {
        if (columnType == null) return "\"sample text\"";

        switch (columnType.toLowerCase()) {
            case "int":
            case "integer":
            case "bigint":
            case "smallint":
            case "tinyint":
                return "123";
            case "double":
            case "float":
            case "decimal":
            case "numeric":
            case "real":
                return "123.45";
            case "boolean":
            case "bool":
            case "bit":
                return "true";
            case "date":
                return "\"2023-12-25\"";
            case "datetime":
            case "timestamp":
                return "\"2023-12-25T10:30:00Z\"";
            case "time":
                return "\"10:30:00\"";
            default:
                return "\"sample text\"";
        }
    }
}
