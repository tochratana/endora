package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;
import rinsanom.com.springtwodatasoure.entity.TableSchema;
import rinsanom.com.springtwodatasoure.repository.mongo.EndpointDocumentationRepository;
import rinsanom.com.springtwodatasoure.service.DynamicEndpointService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(name = "org.springframework.data.mongodb.core.MongoTemplate")
public class DynamicEndpointServiceImpl implements DynamicEndpointService {

    private final EndpointDocumentationRepository endpointDocumentationRepository;

    @Override
    public void generateEndpointsForTable(TableSchema tableSchema) {
        String tableName = tableSchema.getSchemaName();
        String projectId = tableSchema.getProjectId();

        try {
            String documentation;

            // Check if this is a users table - generate auth documentation instead
            if (tableName.equalsIgnoreCase("user") || tableName.equalsIgnoreCase("users")) {
                log.info("Generating authentication endpoint documentation for users table: {}", tableName);
                documentation = generateAuthenticationDocumentation(tableSchema);
            } else {
                log.info("Generating standard CRUD endpoint documentation for table: {}", tableName);
                documentation = generateStandardDocumentation(tableName, tableSchema);
            }

            // Check if documentation already exists
            Optional<EndpointDocumentation> existingDoc = endpointDocumentationRepository
                    .findBySchemaNameAndProjectId(tableName, projectId);

            EndpointDocumentation endpointDoc;
            if (existingDoc.isPresent()) {
                // Update existing documentation
                endpointDoc = existingDoc.get();
                endpointDoc.setRawDocumentation(documentation);
                endpointDoc.setTableSchema(tableSchema.getSchema());
                endpointDoc.setUpdatedAt();
                log.info("Updating existing endpoint documentation for table: {} in project: {}", tableName, projectId);
            } else {
                // Create new documentation
                endpointDoc = new EndpointDocumentation(tableName, projectId, documentation);
                endpointDoc.setTableSchema(tableSchema.getSchema());
                log.info("Creating new endpoint documentation for table: {} in project: {}", tableName, projectId);
            }

            // Save to MongoDB
            endpointDocumentationRepository.save(endpointDoc);

            System.out.println("=== AUTO-GENERATED ENDPOINTS FOR TABLE: " + tableName.toUpperCase() + " ===");
            System.out.println(documentation);
            System.out.println("=== SAVED TO MONGODB ===\n");

        } catch (Exception e) {
            log.error("Failed to generate endpoints for table: {} in project: {}", tableName, projectId, e);
            throw new RuntimeException("Failed to generate endpoints: " + e.getMessage(), e);
        }
    }

    /**
     * Generate authentication-specific documentation for users table
     */
    private String generateAuthenticationDocumentation(TableSchema tableSchema) {
        String tableName = tableSchema.getSchemaName();
        String projectId = tableSchema.getProjectId();
        String authBasePath = "/client-api/" + projectId + "/auth";
        String apiBasePath = "/client-api/" + projectId;

        StringBuilder doc = new StringBuilder();

        doc.append("Table: ").append(tableName).append(" (Authentication Enabled)\n");
        doc.append("Project ID: ").append(projectId).append("\n");
        doc.append("Schema: ").append(tableSchema.getSchema()).append("\n\n");

        doc.append("Authentication Endpoints:\n");
        doc.append("========================\n\n");

        // Register endpoint
        doc.append("1. POST ").append(authBasePath).append("/register\n");
        doc.append("   Description: Register a new user account\n");
        doc.append("   Content-Type: application/json\n");
        doc.append("   Security: Public (no authentication required)\n");
        doc.append("   Tags: Authentication\n");
        doc.append("   Request Body:\n");
        doc.append("   {\n");
        doc.append("     \"email\": \"user@example.com\",\n");
        doc.append("     \"password\": \"securePassword123\",\n");
        // Add other user fields from schema (excluding auth fields)
        for (Map.Entry<String, String> entry : tableSchema.getSchema().entrySet()) {
            String field = entry.getKey();
            if (!isAuthField(field)) {
                doc.append("     \"").append(field).append("\": ").append(generateExampleValue(entry.getValue())).append(",\n");
            }
        }
        doc.setLength(doc.length() - 2); // Remove trailing comma
        doc.append("\n   }\n");
        doc.append("   Success Response (200):\n");
        doc.append("   {\n");
        doc.append("     \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n");
        doc.append("     \"user\": {\n");
        doc.append("       \"id\": \"507f1f77bcf86cd799439011\",\n");
        doc.append("       \"email\": \"user@example.com\",\n");
        doc.append("       \"createdAt\": \"2023-12-01T10:30:00Z\"\n");
        doc.append("     }\n");
        doc.append("   }\n");
        doc.append("   Example: curl -X POST http://localhost:8080").append(authBasePath).append("/register");
        doc.append(" -H \"Content-Type: application/json\" -d '{\"email\":\"user@example.com\",\"password\":\"securePassword123\"}'\n\n");

        // Login endpoint
        doc.append("2. POST ").append(authBasePath).append("/login\n");
        doc.append("   Description: Login with email and password to receive JWT token\n");
        doc.append("   Content-Type: application/json\n");
        doc.append("   Security: Public (no authentication required)\n");
        doc.append("   Tags: Authentication\n");
        doc.append("   Request Body:\n");
        doc.append("   {\n");
        doc.append("     \"email\": \"user@example.com\",\n");
        doc.append("     \"password\": \"securePassword123\"\n");
        doc.append("   }\n");
        doc.append("   Success Response (200):\n");
        doc.append("   {\n");
        doc.append("     \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n");
        doc.append("     \"user\": {\n");
        doc.append("       \"id\": \"507f1f77bcf86cd799439011\",\n");
        doc.append("       \"email\": \"user@example.com\",\n");
        doc.append("       \"createdAt\": \"2023-12-01T10:30:00Z\"\n");
        doc.append("     }\n");
        doc.append("   }\n");
        doc.append("   Example: curl -X POST http://localhost:8080").append(authBasePath).append("/login");
        doc.append(" -H \"Content-Type: application/json\" -d '{\"email\":\"user@example.com\",\"password\":\"securePassword123\"}'\n\n");

        // Get current user endpoint
        doc.append("3. GET ").append(authBasePath).append("/me\n");
        doc.append("   Description: Get current authenticated user information\n");
        doc.append("   Security: Bearer Token Required\n");
        doc.append("   Tags: Authentication\n");
        doc.append("   Headers:\n");
        doc.append("     Authorization: Bearer {token}\n");
        doc.append("   Success Response (200):\n");
        doc.append("   {\n");
        doc.append("     \"id\": \"507f1f77bcf86cd799439011\",\n");
        doc.append("     \"email\": \"user@example.com\",\n");
        doc.append("     \"createdAt\": \"2023-12-01T10:30:00Z\"\n");
        doc.append("   }\n");
        doc.append("   Example: curl -X GET http://localhost:8080").append(authBasePath).append("/me");
        doc.append(" -H \"Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n\n");

        doc.append("Protected API Endpoints (Require Authentication):\n");
        doc.append("=================================================\n\n");

        // Standard CRUD endpoints but marked as protected
        doc.append("4. GET ").append(apiBasePath).append("/").append(tableName).append("\n");
        doc.append("   Description: Get all user records (Admin/Protected access)\n");
        doc.append("   Security: Bearer Token Required\n");
        doc.append("   Tags: Users Management\n");
        doc.append("   Headers:\n");
        doc.append("     Authorization: Bearer {token}\n");
        doc.append("   Response: Array of user objects\n\n");

        doc.append("5. GET ").append(apiBasePath).append("/").append(tableName).append("/{id}\n");
        doc.append("   Description: Get specific user record by ID\n");
        doc.append("   Security: Bearer Token Required\n");
        doc.append("   Tags: Users Management\n");
        doc.append("   Headers:\n");
        doc.append("     Authorization: Bearer {token}\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n\n");

        doc.append("6. PUT ").append(apiBasePath).append("/").append(tableName).append("/{id}\n");
        doc.append("   Description: Update user record by ID\n");
        doc.append("   Security: Bearer Token Required\n");
        doc.append("   Tags: Users Management\n");
        doc.append("   Headers:\n");
        doc.append("     Authorization: Bearer {token}\n");
        doc.append("   Content-Type: application/json\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n\n");

        doc.append("7. DELETE ").append(apiBasePath).append("/").append(tableName).append("/{id}\n");
        doc.append("   Description: Delete user record by ID\n");
        doc.append("   Security: Bearer Token Required\n");
        doc.append("   Tags: Users Management\n");
        doc.append("   Headers:\n");
        doc.append("     Authorization: Bearer {token}\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n\n");

        doc.append("Authentication Flow:\n");
        doc.append("===================\n");
        doc.append("1. Register: POST ").append(authBasePath).append("/register\n");
        doc.append("2. Login: POST ").append(authBasePath).append("/login (returns JWT token)\n");
        doc.append("3. Use token: Include 'Authorization: Bearer {token}' header in protected API calls\n");
        doc.append("4. Token expires in 7 days - refresh by logging in again\n\n");

        doc.append("Security Notes:\n");
        doc.append("==============\n");
        doc.append("- JWT tokens are project-specific and cannot be used across different projects\n");
        doc.append("- Passwords are automatically hashed using BCrypt\n");
        doc.append("- Email addresses must be unique within the project\n");
        doc.append("- All protected endpoints require valid JWT token in Authorization header\n");

        return doc.toString();
    }

    /**
     * Generate standard CRUD documentation for non-users tables
     */
    private String generateStandardDocumentation(String tableName, TableSchema tableSchema) {
        String basePath = "/api/tables/" + tableName;
        StringBuilder doc = new StringBuilder();

        doc.append("Table: ").append(tableName).append("\n");
        doc.append("Project ID: ").append(tableSchema.getProjectId()).append("\n");
        doc.append("Schema: ").append(tableSchema.getSchema()).append("\n\n");

        doc.append("Available Endpoints:\n");
        doc.append("==================\n\n");

        // Generate detailed request body schema
        String detailedSchema = generateDetailedRequestBodySchema(tableSchema.getSchema(), tableName);
        String exampleRequestBody = generateExampleJson(tableSchema.getSchema());

        // GET all records
        doc.append("1. GET ").append(basePath).append("\n");
        doc.append("   Description: Get all records from ").append(tableName).append(" table\n");
        doc.append("   Response: Array of objects matching the table schema\n");
        doc.append("   Response Schema: ").append(exampleRequestBody).append("\n");
        doc.append("   Example: curl -X GET http://localhost:8080").append(basePath).append("\n\n");

        // POST create record
        doc.append("2. POST ").append(basePath).append("\n");
        doc.append("   Description: Create a new record in ").append(tableName).append(" table\n");
        doc.append("   Content-Type: application/json\n");
        doc.append("   Required Fields: ").append(getRequiredFields(tableSchema.getSchema())).append("\n");
        doc.append("   Request Body Schema:\n").append(detailedSchema).append("\n");
        doc.append("   Example Request Body: ").append(exampleRequestBody).append("\n");
        doc.append("   Note: projectId is required in request body\n");
        doc.append("   Example: curl -X POST http://localhost:8080").append(basePath)
                .append(" -H \"Content-Type: application/json\" -d '").append(exampleRequestBody).append("'\n\n");

        // GET by ID
        doc.append("3. GET ").append(basePath).append("/{id}\n");
        doc.append("   Description: Get a specific record by ID\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n");
        doc.append("   Response Schema: ").append(exampleRequestBody).append("\n");
        doc.append("   Example: curl -X GET http://localhost:8080").append(basePath).append("/507f1f77bcf86cd799439011\n\n");

        // PUT update
        doc.append("4. PUT ").append(basePath).append("/{id}\n");
        doc.append("   Description: Update a specific record by ID\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n");
        doc.append("   Content-Type: application/json\n");
        doc.append("   Request Body: JSON object with fields to update (partial update supported)\n");
        doc.append("   Update Schema:\n").append(detailedSchema).append("\n");
        doc.append("   Example Update Body: ").append(generatePartialUpdateExample(tableSchema.getSchema())).append("\n");
        doc.append("   Example: curl -X PUT http://localhost:8080").append(basePath)
                .append("/507f1f77bcf86cd799439011 -H \"Content-Type: application/json\" -d '").append(generatePartialUpdateExample(tableSchema.getSchema())).append("'\n\n");

        // DELETE
        doc.append("5. DELETE ").append(basePath).append("/{id}\n");
        doc.append("   Description: Delete a specific record by ID\n");
        doc.append("   Parameters: id (path parameter - MongoDB ObjectId)\n");
        doc.append("   Response: Success/Error message\n");
        doc.append("   Example: curl -X DELETE http://localhost:8080").append(basePath).append("/507f1f77bcf86cd799439011\n\n");

        // Add validation rules section
        doc.append("Validation Rules:\n");
        doc.append("=================\n");
        doc.append(generateValidationRules(tableSchema.getSchema())).append("\n");

        return doc.toString();
    }

    /**
     * Check if a field is an authentication-related field
     */
    private boolean isAuthField(String fieldName) {
        return fieldName.equals("id") ||
                fieldName.equals("password_hash") ||
                fieldName.equals("created_at") ||
                fieldName.equals("updated_at");
    }

    @Override
    public void removeEndpointsForTable(String tableName) {
        try {
            endpointDocumentationRepository.deleteBySchemaName(tableName);
            log.info("Removed endpoint documentation for table: {}", tableName);
            System.out.println("Removed endpoints documentation for table: " + tableName);
        } catch (Exception e) {
            log.error("Failed to remove endpoint documentation for table: {}", tableName, e);
            throw new RuntimeException("Failed to remove endpoint documentation: " + e.getMessage(), e);
        }
    }

    @Override
    public String getEndpointDocumentation(String tableName) {
        Optional<EndpointDocumentation> doc = endpointDocumentationRepository.findBySchemaName(tableName);
        return doc.map(EndpointDocumentation::getRawDocumentation)
                .orElse("No documentation found for table: " + tableName);
    }

    @Override
    public Map<String, String> getAllEndpointDocumentation() {
        List<EndpointDocumentation> allDocs = endpointDocumentationRepository.findAllByOrderByCreatedAtDesc();
        Map<String, String> result = new HashMap<>();

        for (EndpointDocumentation doc : allDocs) {
            result.put(doc.getSchemaName(), doc.getRawDocumentation());
        }

        return result;
    }

    // Additional methods for enhanced functionality
    public EndpointDocumentation getEndpointDocumentationEntity(String tableName) {
        return endpointDocumentationRepository.findBySchemaName(tableName).orElse(null);
    }

    public EndpointDocumentation getEndpointDocumentationByTableAndProject(String tableName, String projectId) {
        return endpointDocumentationRepository.findBySchemaNameAndProjectId(tableName, projectId).orElse(null);
    }

    public List<EndpointDocumentation> getEndpointDocumentationByProject(String projectId) {
        return endpointDocumentationRepository.findByProjectId(projectId);
    }

    public void removeEndpointsForTableAndProject(String tableName, String projectId) {
        try {
            endpointDocumentationRepository.deleteBySchemaNameAndProjectId(tableName, projectId);
            log.info("Removed endpoint documentation for table: {} in project: {}", tableName, projectId);
        } catch (Exception e) {
            log.error("Failed to remove endpoint documentation for table: {} in project: {}", tableName, projectId, e);
            throw new RuntimeException("Failed to remove endpoint documentation: " + e.getMessage(), e);
        }
    }

    private String generateDetailedRequestBodySchema(Map<String, String> schema, String tableName) {
        StringBuilder detailedSchema = new StringBuilder();
        detailedSchema.append("{\n");

        for (Map.Entry<String, String> entry : schema.entrySet()) {
            String columnName = entry.getKey();
            String dataType = entry.getValue().toUpperCase();

            detailedSchema.append("  \"").append(columnName).append("\": {\n");
            detailedSchema.append("    \"type\": \"").append(mapDataTypeToJsonSchema(dataType)).append("\",\n");

            // Add validation rules based on data type
            if (dataType.contains("VARCHAR") || dataType.contains("TEXT")) {
                detailedSchema.append("    \"maxLength\": 255,\n");
            } else if (dataType.contains("INT") || dataType.contains("BIGINT")) {
                detailedSchema.append("    \"minimum\": 0,\n");
            } else if (dataType.contains("DECIMAL") || dataType.contains("FLOAT")) {
                detailedSchema.append("    \"minimum\": 0.0,\n");
            } else if (dataType.contains("BOOLEAN")) {
                // No additional properties for boolean
            } else if (dataType.contains("DATE")) {
                detailedSchema.append("    \"format\": \"date\",\n");
            }

            // Add example value
            detailedSchema.append("    \"example\": ").append(generateExampleValue(dataType)).append("\n");
            detailedSchema.append("  },\n");
        }

        // Remove trailing comma and close the object
        if (detailedSchema.length() > 2) {
            detailedSchema.setLength(detailedSchema.length() - 2);
        }
        detailedSchema.append("\n}");

        return detailedSchema.toString();
    }

    private String mapDataTypeToJsonSchema(String dataType) {
        switch (dataType.toUpperCase()) {
            case "VARCHAR":
            case "TEXT":
                return "string";
            case "INT":
            case "BIGINT":
                return "integer";
            case "DECIMAL":
            case "FLOAT":
                return "number";
            case "BOOLEAN":
                return "boolean";
            case "DATE":
                return "string"; // Date as string with format
            default:
                return "string"; // Default to string for unknown types
        }
    }

    private String generateExampleValue(String dataType) {
        switch (dataType.toUpperCase()) {
            case "VARCHAR":
            case "TEXT":
                return "\"example_value\"";
            case "INT":
            case "BIGINT":
                return "123";
            case "DECIMAL":
            case "FLOAT":
                return "123.45";
            case "BOOLEAN":
                return "true";
            case "DATE":
                return "\"2023-12-01\"";
            default:
                return "\"example_value\"";
        }
    }

    private String generatePartialUpdateExample(Map<String, String> schema) {
        StringBuilder json = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            if (count > 0) json.append(", ");
            json.append("\"").append(entry.getKey()).append("\": ");

            // Generate example values based on data type
            String dataType = entry.getValue().toUpperCase();
            if (dataType.contains("VARCHAR") || dataType.contains("TEXT")) {
                json.append("\"updated_example_").append(entry.getKey()).append("\"");
            } else if (dataType.contains("INT") || dataType.contains("BIGINT")) {
                json.append("456");
            } else if (dataType.contains("DECIMAL") || dataType.contains("FLOAT")) {
                json.append("456.78");
            } else if (dataType.contains("BOOLEAN")) {
                json.append("false");
            } else if (dataType.contains("DATE")) {
                json.append("\"2023-12-31\"");
            } else {
                json.append("\"updated_example_value\"");
            }
            count++;
        }
        json.append("}");
        return json.toString();
    }

    private String getRequiredFields(Map<String, String> schema) {
        List<String> requiredFields = new ArrayList<>();
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            // Assuming VARCHAR and INT fields are required by default
            String dataType = entry.getValue().toUpperCase();
            if (dataType.contains("VARCHAR") || dataType.contains("INT")) {
                requiredFields.add(entry.getKey());
            }
        }
        return String.join(", ", requiredFields);
    }

    private String generateValidationRules(Map<String, String> schema) {
        StringBuilder rules = new StringBuilder();
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            String columnName = entry.getKey();
            String dataType = entry.getValue().toUpperCase();

            rules.append("- ").append(columnName).append(": ");

            // Add validation rules based on data type
            if (dataType.contains("VARCHAR") || dataType.contains("TEXT")) {
                rules.append("Max length 255 characters");
            } else if (dataType.contains("INT") || dataType.contains("BIGINT")) {
                rules.append("Must be a positive integer");
            } else if (dataType.contains("DECIMAL") || dataType.contains("FLOAT")) {
                rules.append("Must be a positive number");
            } else if (dataType.contains("BOOLEAN")) {
                rules.append("Must be true or false");
            } else if (dataType.contains("DATE")) {
                rules.append("Must be a valid date in the format YYYY-MM-DD");
            } else {
                rules.append("No specific validation");
            }
            rules.append("\n");
        }
        return rules.toString();
    }

    private String generateExampleJson(Map<String, String> schema) {
        StringBuilder json = new StringBuilder("{");

        // Always include projectId as it's required
        json.append("\"projectId\": \"example-project-123\"");

        int count = 1; // Start at 1 since we already added projectId
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            if (count > 0) json.append(", ");
            json.append("\"").append(entry.getKey()).append("\": ");

            // Generate realistic example values based on data type
            String dataType = entry.getValue().toUpperCase();
            String columnName = entry.getKey().toLowerCase();

            if (dataType.contains("VARCHAR") || dataType.contains("TEXT")) {
                // Generate contextual examples based on column name
                if (columnName.contains("name")) {
                    json.append("\"John Doe\"");
                } else if (columnName.contains("email")) {
                    json.append("\"john.doe@example.com\"");
                } else if (columnName.contains("phone")) {
                    json.append("\"+1-555-0123\"");
                } else if (columnName.contains("address")) {
                    json.append("\"123 Main St, City, State 12345\"");
                } else if (columnName.contains("description")) {
                    json.append("\"A detailed description of the item\"");
                } else if (columnName.contains("title")) {
                    json.append("\"Sample Title\"");
                } else if (columnName.contains("status")) {
                    json.append("\"active\"");
                } else {
                    json.append("\"example_").append(entry.getKey()).append("\"");
                }
            } else if (dataType.contains("INT") || dataType.contains("BIGINT")) {
                // Generate contextual numbers
                if (columnName.contains("age")) {
                    json.append("25");
                } else if (columnName.contains("count") || columnName.contains("quantity")) {
                    json.append("10");
                } else if (columnName.contains("price") || columnName.contains("amount")) {
                    json.append("100");
                } else {
                    json.append("123");
                }
            } else if (dataType.contains("DECIMAL") || dataType.contains("FLOAT")) {
                // Generate contextual decimal numbers
                if (columnName.contains("price") || columnName.contains("amount") || columnName.contains("cost")) {
                    json.append("99.99");
                } else if (columnName.contains("rating") || columnName.contains("score")) {
                    json.append("4.5");
                } else if (columnName.contains("percentage") || columnName.contains("percent")) {
                    json.append("85.5");
                } else {
                    json.append("123.45");
                }
            } else if (dataType.contains("BOOLEAN")) {
                // Generate contextual boolean values
                if (columnName.contains("active") || columnName.contains("enabled")) {
                    json.append("true");
                } else if (columnName.contains("deleted") || columnName.contains("disabled")) {
                    json.append("false");
                } else {
                    json.append("true");
                }
            } else if (dataType.contains("DATE")) {
                // Generate contextual dates
                if (columnName.contains("birth") || columnName.contains("born")) {
                    json.append("\"1990-01-15\"");
                } else if (columnName.contains("created") || columnName.contains("start")) {
                    json.append("\"2023-12-01\"");
                } else if (columnName.contains("updated") || columnName.contains("modified")) {
                    json.append("\"2023-12-15\"");
                } else if (columnName.contains("end") || columnName.contains("expired")) {
                    json.append("\"2024-12-31\"");
                } else {
                    json.append("\"2023-12-01\"");
                }
            } else {
                json.append("\"example_value\"");
            }
            count++;
        }
        json.append("}");
        return json.toString();
    }
}