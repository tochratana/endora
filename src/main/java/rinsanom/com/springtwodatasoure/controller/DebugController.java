package rinsanom.com.springtwodatasoure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(Authentication authentication) {
        Map<String, Object> tokenInfo = new HashMap<>();

        if (authentication != null) {
            tokenInfo.put("authenticated", authentication.isAuthenticated());
            tokenInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());
            tokenInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                tokenInfo.put("subject", jwt.getSubject());
                tokenInfo.put("issuer", jwt.getIssuer());
                tokenInfo.put("claims", jwt.getClaims());
            }
        } else {
            tokenInfo.put("authenticated", false);
            tokenInfo.put("message", "No authentication found");
        }

        return ResponseEntity.ok(tokenInfo);
    }

    @GetMapping("/test-user-role")
    public ResponseEntity<String> testUserRole(Authentication authentication) {
        return ResponseEntity.ok("User role endpoint accessed successfully by: " +
                authentication.getName());
    }

    @GetMapping("/test-admin-role")
    public ResponseEntity<String> testAdminRole(Authentication authentication) {
        return ResponseEntity.ok("Admin role endpoint accessed successfully by: " +
                authentication.getName());
    }
}