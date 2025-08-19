package rinsanom.com.springtwodatasoure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "schemas_records")
public class TableData {
    @Id
    private String id;
    private String schemaName;
    private String projectId;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableData() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TableData(String schemaName, String projectId, Map<String, Object> data) {
        this();
        this.schemaName = schemaName;
        this.projectId = projectId;
        this.data = data;
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
