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
                // Auth endpoints - permit all (make sure these are at the top)
                .requestMatchers("/api/auth/**").permitAll()

                // Test endpoints for debugging
                .requestMatchers("/api/test/public").permitAll()
                .requestMatchers("/api/test/protected").authenticated()
                .requestMatchers("/api/test/user-only").hasAnyRole(ROLE_USER)

                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole(ROLE_ADMIN)

                // Create Table role endpoints
                .requestMatchers(HttpMethod.GET, "/table").hasAnyRole(ROLE_USER)

                // Endpoint role endpoints
                .requestMatchers(HttpMethod.POST, "/api/endpoints").hasAnyRole(ROLE_USER)

                // Swagger or postman for user
                .requestMatchers(HttpMethod.POST, "/api/openapi").hasAnyRole(ROLE_USER)

                // Project
                .requestMatchers(HttpMethod.POST, "/projects").hasAnyRole(ROLE_USER)

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
            try {
                // Log JWT claims for debugging
                System.out.println("JWT Claims: " + jwt.getClaims());
                System.out.println("JWT Subject: " + jwt.getSubject());
                System.out.println("JWT Issuer: " + jwt.getIssuer());

                // Try to get realm_access roles
                Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
                System.out.println("Realm Access: " + realmAccess);

                Collection<String> roles = realmAccess != null ? realmAccess.get("roles") : null;
                System.out.println("Extracted Roles: " + roles);

                if (roles == null || roles.isEmpty()) {
                    // If no realm roles, try resource_access or assign default role
                    System.out.println("No realm roles found, assigning default USER role");
                    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }

                Collection<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                System.out.println("Final Authorities: " + authorities);
                return authorities;

            } catch (Exception e) {
                System.err.println("Error extracting roles from JWT: " + e.getMessage());
                e.printStackTrace();
                // Return default role on error
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }
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
