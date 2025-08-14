package rinsanom.com.springtwodatasoure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Projects {
    @Id
    private String id;
    private String userId;
    private String projectName;
    private String description;
}
