package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateTableRequestDTO {
    private String schemaName;
    private Map<String, String> schema;
}
