package rinsanom.com.springtwodatasoure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "table_data")
public class TableData {
    @Id
    private String id;
    private String tableName;
    private String projectId;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableData() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TableData(String tableName, String projectId, Map<String, Object> data) {
        this();
        this.tableName = tableName;
        this.projectId = projectId;
        this.data = data;
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
