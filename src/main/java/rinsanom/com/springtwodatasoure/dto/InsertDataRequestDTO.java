package rinsanom.com.springtwodatasoure.dto;

import lombok.Data;
import java.util.Map;

@Data
public class InsertDataRequestDTO {
    private String tableName;
    private String projectId;
    private Map<String, Object> data; // Column name -> value mapping
}
