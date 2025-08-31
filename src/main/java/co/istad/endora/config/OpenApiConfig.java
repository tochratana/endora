package co.istad.endora.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dynamic Table API")
                        .version("1.0.0")
                        .description("Auto-generated APIs for dynamic schemas"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Development server"));
    }
}
