package rinsanom.com.springtwodatasoure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "rinsanom.com.springtwodatasoure.repository.mongo")
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
public class MongoConfig {
    // MongoDB repositories only enabled when MongoDB URI is provided
    // This allows the application to start without MongoDB configuration
}
