package rinsanom.com.springtwodatasoure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "endpoint_documentation")
public class EndpointDocumentation {
    @Id
    private String id;
    private String tableName;
    private String projectId;
    private String rawDocumentation;
    private Map<String, Object> structuredDocumentation;
    private List<EndpointInfo> endpoints;
    private Map<String, String> tableSchema;
    private String basePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EndpointDocumentation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public EndpointDocumentation(String tableName, String projectId, String rawDocumentation) {
        this();
        this.tableName = tableName;
        this.projectId = projectId;
        this.rawDocumentation = rawDocumentation;
        this.basePath = "/api/tables/" + tableName;
    }

    @Data
    public static class EndpointInfo {
        private String method;
        private String path;
        private String fullUrl;
        private String description;
        private String responseType;
        private String requestBody;
        private String parameters;
        private String exampleSchema;
        private String curlExample;
    }

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
