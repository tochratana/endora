package rinsanom.com.springtwodatasoure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Projects {
    @Id
    private String id;
    private String userUuid;
    private String projectUuid;
    private String projectName;
    private String description;

    // authentication fields
    private boolean authenticationEnabled = false;
    private String jwtSecret;
    private String userTableName = "users";

    // Constructor for creating new projects
//    public Projects(String userUuid, String projectName, String description) {
//        this.userUuid = userUuid;
//        this.projectName = projectName;
//        this.description = description;
//        this.projectUuid = UUID.randomUUID().toString();
//    }
}
