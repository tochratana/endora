package rinsanom.com.springtwodatasoure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@Slf4j
@Profile("prod")
public class ProductionConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.data.mongodb.uri")
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * Health check bean that validates database connectivity without failing startup
     */
    @Bean
    public DatabaseHealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return new DatabaseHealthIndicator(dataSource);
    }

    public static class DatabaseHealthIndicator {
        private final DataSource dataSource;

        public DatabaseHealthIndicator(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public boolean isHealthy() {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(5);
            } catch (Exception e) {
                log.warn("Database health check failed: {}", e.getMessage());
                return false;
            }
        }
    }
}
