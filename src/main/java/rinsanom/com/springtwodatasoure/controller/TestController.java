package rinsanom.com.springtwodatasoure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "This is a public endpoint",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(Authentication authentication) {
        log.info("Protected endpoint accessed by: {}", authentication.getName());
        log.info("Authorities: {}", authentication.getAuthorities());

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            log.info("JWT Claims: {}", jwt.getClaims());

            return ResponseEntity.ok(Map.of(
                "message", "This is a protected endpoint",
                "user", authentication.getName(),
                "authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()),
                "jwt_subject", jwt.getSubject(),
                "jwt_claims", jwt.getClaims(),
                "timestamp", System.currentTimeMillis()
            ));
        }

        return ResponseEntity.ok(Map.of(
            "message", "This is a protected endpoint",
            "user", authentication.getName(),
            "authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()),
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/user-only")
    public ResponseEntity<Map<String, Object>> userOnlyEndpoint(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "This endpoint requires USER role",
            "user", authentication.getName(),
            "authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()),
            "timestamp", System.currentTimeMillis()
        ));
    }
}
