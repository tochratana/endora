package rinsanom.com.springtwodatasoure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "table_schemas")
public class TableSchema {
    @Id
    private String id;
    private String tableName;
    private String projectId;
    private Map<String, String> schema;
    private List<TableRelationship> relationships; // New field for relationships
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableSchema() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TableSchema(String tableName, String projectId, Map<String, String> schema) {
        this();
        this.tableName = tableName;
        this.projectId = projectId;
        this.schema = schema;
    }

    public TableSchema(String tableName, String projectId, Map<String, String> schema, List<TableRelationship> relationships) {
        this(tableName, projectId, schema);
        this.relationships = relationships;
    }

    @Data
    public static class TableRelationship {
        private String foreignKeyColumn;
        private String referencedTable;
        private String referencedColumn;
        private String onDelete;
        private String onUpdate;
    }
}
