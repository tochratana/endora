package co.istad.endora.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize annotations
@RequiredArgsConstructor
public class KeycloakSecurityConfig {

    // Role constants
    private final String ROLE_ADMIN = "ADMIN";
    private final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain configureApiSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(endpoint -> endpoint
                // Public endpoints - permit all
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll() // Health checks, etc.
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // API documentation

                // Debug endpoints for testing (remove in production)
                .requestMatchers("/api/debug/**").authenticated()
                .requestMatchers("/api/debug/test-user-role").hasAnyRole(ROLE_USER)
                .requestMatchers("/api/debug/test-admin-role").hasAnyRole(ROLE_ADMIN)

                // ADMIN ENDPOINTS - Only admins can access
                .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)

                // Admin user management endpoints
                .requestMatchers(HttpMethod.GET, "/api/admin/users").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/admin/users/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/admin/users/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/admin/users/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/admin/users/**").hasRole(ROLE_ADMIN)

                // USER PROFILE ENDPOINTS - Any authenticated user
                .requestMatchers("/api/profile/**").authenticated()

                // User profile endpoints - specific permissions
                .requestMatchers(HttpMethod.GET, "/api/profile/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/profile/me").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/profile/me/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/profile/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/profile/user/**").authenticated() // Public profiles

                // Legacy endpoints (keep your existing ones)
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole(ROLE_ADMIN) // Legacy admin endpoint
                .requestMatchers(HttpMethod.GET, "/table").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/endpoints").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/openapi").authenticated()
                .requestMatchers(HttpMethod.POST, "/projects").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated());

        // Disable CSRF for API endpoints
        http.csrf(csrf -> csrf.disable());

        // Configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Stateless session management
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
            // Get realm roles
            Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
            Collection<String> realmRoles = realmAccess != null ? realmAccess.get("roles") : java.util.Collections.emptyList();

            // Get client roles (resource_access)
            Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim("resource_access");
            Collection<String> clientRoles = java.util.Collections.emptyList();

            // Extract client roles if they exist (replace "your-client-id" with your actual client ID)
            if (resourceAccess != null) {
                // You can get client roles for your specific client
                Map<String, Collection<String>> clientAccess = resourceAccess.get("your-client-id");
                if (clientAccess != null) {
                    clientRoles = clientAccess.get("roles");
                    if (clientRoles == null) {
                        clientRoles = java.util.Collections.emptyList();
                    }
                }
            }

            // Combine realm and client roles
            Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();

            // Add realm roles with ROLE_ prefix
            if (realmRoles != null) {
                authorities.addAll(realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
            }

            // Add client roles with ROLE_ prefix
            if (clientRoles != null) {
                authorities.addAll(clientRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
            }

            return authorities;
        };

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:4200", // Angular
                "http://localhost:8080", // Local development
                "https://yourdomain.com" // Production domain
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}