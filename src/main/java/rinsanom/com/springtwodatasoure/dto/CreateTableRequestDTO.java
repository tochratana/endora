package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateTableRequestDTO {
    private String tableName;
    private String projectId;  // Changed from Integer to String
    private Map<String, String> schema;
}
