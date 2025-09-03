package rinsanom.com.springtwodatasoure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "rinsanom.com.springtwodatasoure.repository.mongo")
public class MongoConfig {
    // MongoDB repositories enabled for connecting to your deployed MongoDB Atlas cluster
}
