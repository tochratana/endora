package rinsanom.com.springtwodatasoure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rinsanom.com.springtwodatasoure.security.ProjectJwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ProjectJwtAuthenticationFilter projectJwtAuthenticationFilter;

    @Bean
    @Order(1) // Higher priority than Keycloak config
    public SecurityFilterChain projectApiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/client-api/**") // Only apply to client-api endpoints
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/client-api/*/auth/**").permitAll() // Auth endpoints public
                        .requestMatchers("/client-api/**").permitAll() // Let filter handle authentication
                )
                .addFilterBefore(projectJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}