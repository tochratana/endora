package rinsanom.com.springtwodatasoure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class KeycloakSecurityConfig {
    private final String ROLE_ADMIN = "ADMIN";
    private final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain configureApiSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(endpoint -> endpoint
                // Auth endpoints - permit all
                .requestMatchers("/api/auth/**").permitAll()

                // User endpoints
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole(ROLE_USER)
                .requestMatchers(HttpMethod.GET, "/api/products").hasAnyRole(ROLE_USER)

                // Admin endpoints
                .requestMatchers(HttpMethod.POST, "/api/users").hasAnyRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole(ROLE_ADMIN)

                // All other requests require authentication
                .anyRequest().authenticated());

        http.formLogin(form -> form.disable());
        http.csrf(token -> token.disable());
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Configure OAuth2 Resource Server with JWT
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverterForKeycloak()))
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
            Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
            Collection<String> roles = realmAccess != null ? realmAccess.get("roles") : null;

            if (roles == null) {
                return java.util.Collections.emptyList();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
