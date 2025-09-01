package rinsanom.com.springtwodatasoure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "rinsanom.com.springtwodatasoure.repository.postgrest")
@EntityScan(basePackages = "rinsanom.com.springtwodatasoure.entity")
public class DatabaseConfig {
    // This configuration enables PostgreSQL repositories
    // PostgreSQL: For User entity (authentication, user management)
    // MongoDB configuration moved to separate MongoConfig class with conditional loading
}
