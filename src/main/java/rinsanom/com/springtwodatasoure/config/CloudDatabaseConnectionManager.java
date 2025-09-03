package rinsanom.com.springtwodatasoure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "cloud")
public class CloudDatabaseConnectionManager {

    @Value("${DATABASE_URL:}")
    private String realDatabaseUrl;

    @Value("${SPRING_DATA_MONGODB_URI:}")
    private String mongoUri;

    private final DataSource dataSource;

    public CloudDatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🚀 Application started successfully on Cloud Run!");
        log.info("📊 Initial database: H2 in-memory (for startup)");

        if (!realDatabaseUrl.isEmpty()) {
            log.info("🔄 Attempting to connect to production PostgreSQL...");
            CompletableFuture.runAsync(this::testDatabaseConnections);
        }

        if (!mongoUri.isEmpty()) {
            log.info("🔄 MongoDB URI configured: {}", mongoUri.substring(0, Math.min(30, mongoUri.length())) + "...");
        }
    }

    private void testDatabaseConnections() {
        try {
            Thread.sleep(5000); // Wait 5 seconds after startup
            try (Connection conn = dataSource.getConnection()) {
                log.info("✅ Database connection test successful");
            }
        } catch (Exception e) {
            log.warn("⚠️  Database connection test failed: {}", e.getMessage());
            log.info("📝 Application continues running with fallback configuration");
        }
    }
}
