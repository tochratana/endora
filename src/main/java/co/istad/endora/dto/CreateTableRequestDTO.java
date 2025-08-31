package co.istad.endora.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateTableRequestDTO {
    private String projectUuid;  // Project UUID is still required
    private String schemaName;
    private Map<String, String> schema;
}
