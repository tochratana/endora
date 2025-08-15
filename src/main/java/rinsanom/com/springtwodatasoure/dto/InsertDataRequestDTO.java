package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class InsertDataRequestDTO {
    private String tableName;
    private String projectUuid;  // Changed from projectId to projectUuid
    private String userUuid;     // Added userUuid requirement
    private Map<String, Object> data; // Column name -> value mapping
}
