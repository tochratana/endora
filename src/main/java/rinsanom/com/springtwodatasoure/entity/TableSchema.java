package rinsanom.com.springtwodatasoure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "schemas")
public class TableSchema {
    @Id
    private String id;
    private String schemaName;
    private String projectId;
    private Map<String, String> schema;
    private List<TableRelationship> relationships; // New field for relationships
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableSchema() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.relationships = new ArrayList<>();
    }

    public TableSchema(String schemaName, String projectId, Map<String, String> schema) {
        this();
        this.schemaName = schemaName;
        this.projectId = projectId;
        this.schema = schema;
    }

    public TableSchema(String schemaName, String projectId, Map<String, String> schema, List<TableRelationship> relationships) {
        this(schemaName, projectId, schema);
        this.relationships = relationships != null ? relationships : new ArrayList<>();
    }

    @Data
    public static class TableRelationship {
        private String foreignKeyColumn;
        private String referencedTable;
        private String referencedColumn;
        private String relationshipType; // one-to-one, one-to-many, many-to-many
        private String onDelete;
        private String onUpdate;

        public TableRelationship() {
            this.relationshipType = "one-to-many"; // Default to one-to-many
            this.onDelete = "CASCADE";
            this.onUpdate = "CASCADE";
        }

        public TableRelationship(String foreignKeyColumn, String referencedTable, String referencedColumn) {
            this();
            this.foreignKeyColumn = foreignKeyColumn;
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
        }

        public TableRelationship(String foreignKeyColumn, String referencedTable, String referencedColumn, String relationshipType) {
            this(foreignKeyColumn, referencedTable, referencedColumn);
            this.relationshipType = relationshipType != null ? relationshipType : "one-to-many";
        }
    }
}
