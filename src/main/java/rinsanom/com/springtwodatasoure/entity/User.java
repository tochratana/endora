package rinsanom.com.springtwodatasoure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // store keycloak userId (UUID string)
    @Column(nullable = false, unique = true)
    private String keycloakUserId;

    private String displayName;
    private String profileImage;
    private String preferences; // or use @Lob / JSON column
}
