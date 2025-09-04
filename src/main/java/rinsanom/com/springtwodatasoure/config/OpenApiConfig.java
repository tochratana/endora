package rinsanom.com.springtwodatasoure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Dynamic Table API")
                        .version("1.0.0")
                        .description("Auto-generated APIs for dynamic schemas with JWT Bearer authentication"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT access token (without 'Bearer ' prefix)")));

        // Add custom domain
        openAPI.addServersItem(new Server()
                .url("https://api.api-ngin.oudom.dev" + contextPath)
                .description("Production server (Custom Domain)"));

        // Add production server URL for Cloud Run
        openAPI.addServersItem(new Server()
                .url("https://api-engine-backend-308354822720.asia-southeast1.run.app" + contextPath)
                .description("Production server (Google Cloud Run)"));

        // Add localhost for development
        openAPI.addServersItem(new Server()
                .url("http://localhost:8080" + contextPath)
                .description("Development server"));

        return openAPI;
    }
}
