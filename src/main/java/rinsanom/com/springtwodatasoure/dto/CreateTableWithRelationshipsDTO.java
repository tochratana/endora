package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CreateTableWithRelationshipsDTO {
    private String schemaName;  // Changed from tableName to schemaName
    private String projectUuid;  // Changed from projectId to projectUuid
    private String userUuid;     // Added userUuid requirement
    private Map<String, String> schema;
    private List<TableRelationship> relationships;

    @Data
    public static class TableRelationship {
        private String foreignKeyColumn;      // Column in current table
        private String referencedTable;      // Table being referenced
        private String referencedColumn;     // Column in referenced table (usually 'id')
        private String onDelete;             // CASCADE, SET NULL, RESTRICT
        private String onUpdate;             // CASCADE, SET NULL, RESTRICT

        public TableRelationship() {
            this.referencedColumn = "id";     // Default to 'id'
            this.onDelete = "CASCADE";        // Default behavior
            this.onUpdate = "CASCADE";        // Default behavior
        }
    }
}
