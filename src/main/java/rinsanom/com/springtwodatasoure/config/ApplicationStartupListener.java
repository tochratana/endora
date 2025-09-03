package rinsanom.com.springtwodatasoure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profile = String.join(",", env.getActiveProfiles());

        log.info("=================================================");
        log.info("🚀 Application started successfully!");
        log.info("📍 Port: {}", port);
        log.info("🏷️  Active profiles: {}", profile.isEmpty() ? "default" : profile);
        log.info("🌐 Health check: http://localhost:{}/health", port);
        log.info("=================================================");
    }
}
