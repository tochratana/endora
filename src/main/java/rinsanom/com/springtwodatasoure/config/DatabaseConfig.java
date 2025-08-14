package rinsanom.com.springtwodatasoure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "rinsanom.com.springtwodatasoure.repository.postgrest")
@EnableMongoRepositories(basePackages = "rinsanom.com.springtwodatasoure.repository.mongo")
@EntityScan(basePackages = "rinsanom.com.springtwodatasoure.entity")
public class DatabaseConfig {
    // This configuration enables both PostgreSQL and MongoDB repositories
    // PostgreSQL: For User entity (authentication, user management)
    // MongoDB: For dynamic table schemas and data storage
}
