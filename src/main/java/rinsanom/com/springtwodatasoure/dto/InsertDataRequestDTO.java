package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class InsertDataRequestDTO {
    private String projectUuid;  // Changed from projectId to projectUuid
    private String schemaName;   // Changed from tableName to schemaName
    private Map<String, Object> data; // Column name -> value mapping
}
