package rinsanom.com.springtwodatasoure.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.server-url:http://localhost:9090}")
    private String serverUrl;

    @Value("${keycloak.realm:endora_api}")
    private String realm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.admin.client-secret:eUmu5xJnDDiphTPiy7BAKhC3xNREGZCe}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
