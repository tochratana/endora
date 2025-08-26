package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateTableRequestDTO {
    private String userUuid;     // Added userUuid requirement
    private String projectUuid;  // Changed from projectId to projectUuid
    private String schemaName;
    private Map<String, String> schema;
}
