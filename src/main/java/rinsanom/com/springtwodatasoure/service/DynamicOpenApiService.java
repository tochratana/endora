package rinsanom.com.springtwodatasoure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DynamicOpenApiService {

    private final TableService tableService;

    // Define tables that should have public access (no authentication required)
    private static final Set<String> PUBLIC_TABLES = Set.of("public_info", "announcements");

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
        info.put("description", "Auto-generated REST APIs for your custom tables with authentication");
        openApiSpec.put("info", info);

        // Servers
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = new HashMap<>();
        server.put("url", "http://localhost:8080");
        server.put("description", "Development server");
        servers.add(server);
        openApiSpec.put("servers", servers);

        // Components section for reusable schemas
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> schemas = new HashMap<>();

        // Add authentication-related schemas
        addAuthSchemas(schemas);

        // Add error response schema
        Map<String, Object> errorSchema = new HashMap<>();
        errorSchema.put("type", "object");
        Map<String, Object> errorProperties = new HashMap<>();
        Map<String, Object> messageProperty = new HashMap<>();
        messageProperty.put("type", "string");
        messageProperty.put("description", "Error message");
        errorProperties.put("message", messageProperty);
        errorSchema.put("properties", errorProperties);
        schemas.put("Error", errorSchema);

        // Add schemas for each table
        for (TableSchema table : tables) {
            String tableName = table.getSchemaName();
            schemas.put(tableName, createTableSchemaForComponents(tableName, table));
        }

        // Add security schemes
        Map<String, Object> securitySchemes = createSecuritySchemes();
        components.put("securitySchemes", securitySchemes);
        components.put("schemas", schemas);
        openApiSpec.put("components", components);

        // Add global security requirement (can be overridden per operation)
        List<Map<String, Object>> security = new ArrayList<>();
        Map<String, Object> bearerAuth = new HashMap<>();
        bearerAuth.put("BearerAuth", new ArrayList<>());
        security.add(bearerAuth);
        openApiSpec.put("security", security);

        // Paths
        Map<String, Object> paths = new HashMap<>();

        // Add authentication endpoints
        addAuthenticationPaths(paths);

        // Generate paths for each table
        for (TableSchema table : tables) {
            String tableName = table.getSchemaName();
            generateTablePaths(paths, tableName, table);
        }

        openApiSpec.put("paths", paths);
        return openApiSpec;
    }

    private void addAuthSchemas(Map<String, Object> schemas) {
        // Login request schema
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("type", "object");
        loginRequest.put("required", Arrays.asList("username", "password"));
        Map<String, Object> loginProperties = new HashMap<>();

        Map<String, Object> usernameProperty = new HashMap<>();
        usernameProperty.put("type", "string");
        usernameProperty.put("description", "Username or email");
        usernameProperty.put("example", "user@example.com");
        loginProperties.put("username", usernameProperty);

        Map<String, Object> passwordProperty = new HashMap<>();
        passwordProperty.put("type", "string");
        passwordProperty.put("format", "password");
        passwordProperty.put("description", "User password");
        passwordProperty.put("example", "password123");
        loginProperties.put("password", passwordProperty);

        loginRequest.put("properties", loginProperties);
        schemas.put("LoginRequest", loginRequest);

        // Login response schema
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("type", "object");
        Map<String, Object> loginResponseProperties = new HashMap<>();

        Map<String, Object> tokenProperty = new HashMap<>();
        tokenProperty.put("type", "string");
        tokenProperty.put("description", "JWT access token");
        tokenProperty.put("example", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        loginResponseProperties.put("token", tokenProperty);

        Map<String, Object> refreshTokenProperty = new HashMap<>();
        refreshTokenProperty.put("type", "string");
        refreshTokenProperty.put("description", "JWT refresh token");
        refreshTokenProperty.put("example", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        loginResponseProperties.put("refreshToken", refreshTokenProperty);

        Map<String, Object> expiresInProperty = new HashMap<>();
        expiresInProperty.put("type", "integer");
        expiresInProperty.put("description", "Token expiration time in seconds");
        expiresInProperty.put("example", 3600);
        loginResponseProperties.put("expiresIn", expiresInProperty);

        loginResponse.put("properties", loginResponseProperties);
        schemas.put("LoginResponse", loginResponse);

        // User registration schema
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("type", "object");
        registerRequest.put("required", Arrays.asList("username", "email", "password"));
        Map<String, Object> registerProperties = new HashMap<>();

        registerProperties.put("username", usernameProperty);

        Map<String, Object> emailProperty = new HashMap<>();
        emailProperty.put("type", "string");
        emailProperty.put("format", "email");
        emailProperty.put("description", "User email address");
        emailProperty.put("example", "user@example.com");
        registerProperties.put("email", emailProperty);

        registerProperties.put("password", passwordProperty);

        registerRequest.put("properties", registerProperties);
        schemas.put("RegisterRequest", registerRequest);
    }

    private Map<String, Object> createSecuritySchemes() {
        Map<String, Object> securitySchemes = new HashMap<>();

        // Bearer token authentication
        Map<String, Object> bearerAuth = new HashMap<>();
        bearerAuth.put("type", "http");
        bearerAuth.put("scheme", "bearer");
        bearerAuth.put("bearerFormat", "JWT");
        bearerAuth.put("description", "JWT Bearer token authentication");
        securitySchemes.put("BearerAuth", bearerAuth);

        // API Key authentication (alternative)
        Map<String, Object> apiKeyAuth = new HashMap<>();
        apiKeyAuth.put("type", "apiKey");
        apiKeyAuth.put("in", "header");
        apiKeyAuth.put("name", "X-API-Key");
        apiKeyAuth.put("description", "API key authentication");
        securitySchemes.put("ApiKeyAuth", apiKeyAuth);

        return securitySchemes;
    }

    private void addAuthenticationPaths(Map<String, Object> paths) {
        // Login endpoint
        Map<String, Object> loginPath = new HashMap<>();
        Map<String, Object> loginPost = new HashMap<>();
        loginPost.put("tags", Arrays.asList("Authentication"));
        loginPost.put("summary", "User login");
        loginPost.put("description", "Authenticate user and return JWT token");
        loginPost.put("security", new ArrayList<>()); // No security required for login

        Map<String, Object> loginRequestBody = new HashMap<>();
        loginRequestBody.put("required", true);
        loginRequestBody.put("description", "Login credentials");
        Map<String, Object> loginContent = new HashMap<>();
        Map<String, Object> loginMediaType = new HashMap<>();
        Map<String, Object> loginSchema = new HashMap<>();
        loginSchema.put("$ref", "#/components/schemas/LoginRequest");
        loginMediaType.put("schema", loginSchema);
        loginContent.put("application/json", loginMediaType);
        loginRequestBody.put("content", loginContent);
        loginPost.put("requestBody", loginRequestBody);

        Map<String, Object> loginResponses = new HashMap<>();

        // 200 Success response
        Map<String, Object> loginSuccessResponse = new HashMap<>();
        loginSuccessResponse.put("description", "Login successful");
        Map<String, Object> loginSuccessContent = new HashMap<>();
        Map<String, Object> loginSuccessMediaType = new HashMap<>();
        Map<String, Object> loginSuccessSchema = new HashMap<>();
        loginSuccessSchema.put("$ref", "#/components/schemas/LoginResponse");
        loginSuccessMediaType.put("schema", loginSuccessSchema);
        loginSuccessContent.put("application/json", loginSuccessMediaType);
        loginSuccessResponse.put("content", loginSuccessContent);
        loginResponses.put("200", loginSuccessResponse);

        loginResponses.put("401", createErrorResponse("Invalid credentials"));
        loginResponses.put("400", createErrorResponse("Bad request"));
        loginPost.put("responses", loginResponses);

        loginPath.put("post", loginPost);
        paths.put("/api/auth/login", loginPath);

        // Register endpoint
        Map<String, Object> registerPath = new HashMap<>();
        Map<String, Object> registerPost = new HashMap<>();
        registerPost.put("tags", Arrays.asList("Authentication"));
        registerPost.put("summary", "User registration");
        registerPost.put("description", "Register a new user account");
        registerPost.put("security", new ArrayList<>()); // No security required for registration

        Map<String, Object> registerRequestBody = new HashMap<>();
        registerRequestBody.put("required", true);
        registerRequestBody.put("description", "Registration details");
        Map<String, Object> registerContent = new HashMap<>();
        Map<String, Object> registerMediaType = new HashMap<>();
        Map<String, Object> registerSchema = new HashMap<>();
        registerSchema.put("$ref", "#/components/schemas/RegisterRequest");
        registerMediaType.put("schema", registerSchema);
        registerContent.put("application/json", registerMediaType);
        registerRequestBody.put("content", registerContent);
        registerPost.put("requestBody", registerRequestBody);

        Map<String, Object> registerResponses = new HashMap<>();
        registerResponses.put("201", createSuccessResponse("User registered successfully"));
        registerResponses.put("400", createErrorResponse("Bad request"));
        registerResponses.put("409", createErrorResponse("User already exists"));
        registerPost.put("responses", registerResponses);

        registerPath.put("post", registerPost);
        paths.put("/api/auth/register", registerPath);

        // Token refresh endpoint
        Map<String, Object> refreshPath = new HashMap<>();
        Map<String, Object> refreshPost = new HashMap<>();
        refreshPost.put("tags", Arrays.asList("Authentication"));
        refreshPost.put("summary", "Refresh JWT token");
        refreshPost.put("description", "Refresh expired JWT token using refresh token");

        List<Map<String, Object>> refreshSecurity = new ArrayList<>();
        Map<String, Object> refreshBearerAuth = new HashMap<>();
        refreshBearerAuth.put("BearerAuth", new ArrayList<>());
        refreshSecurity.add(refreshBearerAuth);
        refreshPost.put("security", refreshSecurity);

        Map<String, Object> refreshResponses = new HashMap<>();
        Map<String, Object> refreshSuccessResponse = new HashMap<>();
        refreshSuccessResponse.put("description", "Token refreshed successfully");
        Map<String, Object> refreshSuccessContent = new HashMap<>();
        Map<String, Object> refreshSuccessMediaType = new HashMap<>();
        Map<String, Object> refreshSuccessSchema = new HashMap<>();
        refreshSuccessSchema.put("$ref", "#/components/schemas/LoginResponse");
        refreshSuccessMediaType.put("schema", refreshSuccessSchema);
        refreshSuccessContent.put("application/json", refreshSuccessMediaType);
        refreshSuccessResponse.put("content", refreshSuccessContent);
        refreshResponses.put("200", refreshSuccessResponse);

        refreshResponses.put("401", createErrorResponse("Invalid or expired refresh token"));
        refreshPost.put("responses", refreshResponses);

        refreshPath.put("post", refreshPost);
        paths.put("/api/auth/refresh", refreshPath);
    }

    private void generateTablePaths(Map<String, Object> paths, String tableName, TableSchema tableSchema) {
        String basePath = "/api/tables/" + tableName;
        boolean isPublicTable = PUBLIC_TABLES.contains(tableName.toLowerCase());

        // Base path operations (GET all, POST)
        Map<String, Object> basePathOperations = new HashMap<>();

        // GET all records with query parameters
        Map<String, Object> getAllOperation = new HashMap<>();
        getAllOperation.put("summary", "Get all " + tableName);
        getAllOperation.put("description", "Retrieve all records from " + tableName + " table with optional filtering and pagination");
        getAllOperation.put("tags", Arrays.asList(tableName));
        getAllOperation.put("parameters", createQueryParameters());
        getAllOperation.put("responses", createGetAllResponses(tableName, tableSchema));

        // Add security only if not a public table
        if (!isPublicTable) {
            getAllOperation.put("security", createSecurityRequirement());
        } else {
            getAllOperation.put("security", new ArrayList<>()); // Override global security
        }

        basePathOperations.put("get", getAllOperation);

        // POST new record
        Map<String, Object> postOperation = new HashMap<>();
        postOperation.put("summary", "Create " + tableName);
        postOperation.put("description", "Create a new record in " + tableName + " table");
        postOperation.put("tags", Arrays.asList(tableName));
        postOperation.put("requestBody", createRequestBody(tableName, tableSchema));
        postOperation.put("responses", createPostResponses(tableName, tableSchema));

        // POST operations typically require authentication
        if (!isPublicTable) {
            postOperation.put("security", createSecurityRequirement());
        }

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

        if (!isPublicTable) {
            getByIdOperation.put("security", createSecurityRequirement());
        } else {
            getByIdOperation.put("security", new ArrayList<>());
        }

        idPathOperations.put("get", getByIdOperation);

        // PUT by ID
        Map<String, Object> putOperation = new HashMap<>();
        putOperation.put("summary", "Update " + tableName + " by ID");
        putOperation.put("description", "Update a specific record in " + tableName + " table");
        putOperation.put("tags", Arrays.asList(tableName));
        putOperation.put("parameters", Arrays.asList(createIdParameter()));
        putOperation.put("requestBody", createRequestBody(tableName, tableSchema));
        putOperation.put("responses", createPutResponses(tableName, tableSchema));
        putOperation.put("security", createSecurityRequirement()); // Always require auth for updates
        idPathOperations.put("put", putOperation);

        // DELETE by ID
        Map<String, Object> deleteOperation = new HashMap<>();
        deleteOperation.put("summary", "Delete " + tableName + " by ID");
        deleteOperation.put("description", "Delete a specific record from " + tableName + " table");
        deleteOperation.put("tags", Arrays.asList(tableName));
        deleteOperation.put("parameters", Arrays.asList(createIdParameter()));
        deleteOperation.put("responses", createDeleteResponses());
        deleteOperation.put("security", createSecurityRequirement()); // Always require auth for deletes
        idPathOperations.put("delete", deleteOperation);

        paths.put(idPath, idPathOperations);
    }

    private List<Map<String, Object>> createSecurityRequirement() {
        List<Map<String, Object>> security = new ArrayList<>();
        Map<String, Object> bearerAuth = new HashMap<>();
        bearerAuth.put("BearerAuth", new ArrayList<>());
        security.add(bearerAuth);
        return security;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("description", message);

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> messageProperty = new HashMap<>();
        messageProperty.put("type", "string");
        messageProperty.put("example", message);
        properties.put("message", messageProperty);
        schema.put("properties", properties);

        mediaType.put("schema", schema);
        content.put("application/json", mediaType);
        response.put("content", content);

        return response;
    }

    // ... (keep all the existing methods from your original code)
    // I'll include the essential ones here, but you should keep all your existing methods

    private List<Map<String, Object>> createQueryParameters() {
        List<Map<String, Object>> parameters = new ArrayList<>();

        // Page parameter
        Map<String, Object> pageParam = new HashMap<>();
        pageParam.put("name", "page");
        pageParam.put("in", "query");
        pageParam.put("description", "Page number for pagination (starts from 0)");
        pageParam.put("required", false);
        Map<String, Object> pageSchema = new HashMap<>();
        pageSchema.put("type", "integer");
        pageSchema.put("format", "int32");
        pageSchema.put("default", 0);
        pageSchema.put("minimum", 0);
        pageParam.put("schema", pageSchema);
        parameters.add(pageParam);

        // Size parameter
        Map<String, Object> sizeParam = new HashMap<>();
        sizeParam.put("name", "size");
        sizeParam.put("in", "query");
        sizeParam.put("description", "Number of records per page");
        sizeParam.put("required", false);
        Map<String, Object> sizeSchema = new HashMap<>();
        sizeSchema.put("type", "integer");
        sizeSchema.put("format", "int32");
        sizeSchema.put("default", 20);
        sizeSchema.put("minimum", 1);
        sizeSchema.put("maximum", 100);
        sizeParam.put("schema", sizeSchema);
        parameters.add(sizeParam);

        // Sort parameter
        Map<String, Object> sortParam = new HashMap<>();
        sortParam.put("name", "sort");
        sortParam.put("in", "query");
        sortParam.put("description", "Sort criteria in the format: property(,asc|desc). Example: id,desc");
        sortParam.put("required", false);
        Map<String, Object> sortSchema = new HashMap<>();
        sortSchema.put("type", "string");
        sortParam.put("schema", sortSchema);
        parameters.add(sortParam);

        return parameters;
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
        projectIdProperty.put("example", "proj_12345");
        properties.put("projectId", projectIdProperty);

        if (tableSchema.getSchema() != null) {
            tableSchema.getSchema().forEach((columnName, columnType) -> {
                Map<String, Object> propertySchema = new HashMap<>();
                propertySchema.put("type", convertToOpenApiType(columnType));
                propertySchema.put("description", "Field: " + columnName + " (Type: " + columnType + ")");

                // Add format and examples using helper method
                propertySchema = addExampleAndFormat(columnType, propertySchema);

                properties.put(columnName, propertySchema);
            });
        }

        schema.put("properties", properties);

        // Mark projectId as required
        schema.put("required", Arrays.asList("projectId"));

        return schema;
    }

    private Map<String, Object> createTableSchemaForComponents(String tableName, TableSchema tableSchema) {
        return createTableSchema(tableName, tableSchema); // Same implementation
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
        responses.put("401", createErrorResponse("Unauthorized - Invalid or missing token"));
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
        responses.put("401", createErrorResponse("Unauthorized - Invalid or missing token"));
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
        responses.put("401", createErrorResponse("Unauthorized - Invalid or missing token"));
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
        responses.put("401", createErrorResponse("Unauthorized - Invalid or missing token"));
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
        responses.put("401", createErrorResponse("Unauthorized - Invalid or missing token"));
        responses.put("404", createErrorResponse("Record not found"));
        responses.put("500", createErrorResponse("Internal Server Error"));

        return responses;
    }

    private Map<String, Object> createErrorResponse(String description) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("description", description);

        // Add content with schema reference
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> mediaType = new HashMap<>();
        Map<String, Object> schema = new HashMap<>();
        schema.put("$ref", "#/components/schemas/Error");
        mediaType.put("schema", schema);
        content.put("application/json", mediaType);
        errorResponse.put("content", content);

        return errorResponse;
    }

    private String convertToOpenApiType(String columnType) {
        if (columnType == null) return "string";

        switch (columnType.toLowerCase()) {
            case "int":
            case "integer":
            case "bigint":
            case "smallint":
            case "tinyint":
                return "integer";
            case "double":
            case "float":
            case "decimal":
            case "numeric":
            case "real":
                return "number";
            case "boolean":
            case "bool":
            case "bit":
                return "boolean";
            case "date":
            case "datetime":
            case "timestamp":
            case "time":
                return "string";
            case "text":
            case "longtext":
            case "mediumtext":
            case "varchar":
            case "char":
            case "nvarchar":
            case "nchar":
            default:
                return "string";
        }
    }

    private Map<String, Object> addExampleAndFormat(String columnType, Map<String, Object> propertySchema) {
        String openApiType = convertToOpenApiType(columnType);

        switch (openApiType) {
            case "integer":
                propertySchema.put("format", "int32");
                propertySchema.put("example", 123);
                break;
            case "number":
                propertySchema.put("format", "double");
                propertySchema.put("example", 123.45);
                break;
            case "boolean":
                propertySchema.put("example", true);
                break;
            case "string":
                if (columnType.toLowerCase().contains("date") || columnType.toLowerCase().contains("time")) {
                    if (columnType.toLowerCase().equals("date")) {
                        propertySchema.put("format", "date");
                        propertySchema.put("example", "2023-12-25");
                    } else if (columnType.toLowerCase().contains("datetime") || columnType.toLowerCase().contains("timestamp")) {
                        propertySchema.put("format", "date-time");
                        propertySchema.put("example", "2023-12-25T10:30:00Z");
                    } else if (columnType.toLowerCase().equals("time")) {
                        propertySchema.put("format", "time");
                        propertySchema.put("example", "10:30:00");
                    }
                } else {
                    propertySchema.put("example", "sample text");
                }
                break;
        }

        return propertySchema;
    }
}