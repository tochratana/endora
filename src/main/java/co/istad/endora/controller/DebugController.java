package co.istad.endora.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.istad.endora.security.TokenUserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final TokenUserService tokenUserService;

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        Map<String, Object> tokenInfo = new HashMap<>();

        try {
            Jwt jwt = tokenUserService.getCurrentJwtToken();
            String userUuid = tokenUserService.getCurrentUserUuid();
            String keycloakUserId = tokenUserService.getCurrentKeycloakUserId();

            tokenInfo.put("authenticated", true);
            tokenInfo.put("userUuid", userUuid);
            tokenInfo.put("keycloakUserId", keycloakUserId);
            tokenInfo.put("subject", jwt.getSubject());
            tokenInfo.put("issuer", jwt.getIssuer());
            tokenInfo.put("username", jwt.getClaimAsString("preferred_username"));
            tokenInfo.put("email", jwt.getClaimAsString("email"));
            tokenInfo.put("isAdmin", tokenUserService.isAdmin());
            tokenInfo.put("claims", jwt.getClaims());
        } catch (Exception e) {
            tokenInfo.put("authenticated", false);
            tokenInfo.put("error", e.getMessage());
        }

        return ResponseEntity.ok(tokenInfo);
    }

    @GetMapping("/test-user-role")
    public ResponseEntity<String> testUserRole() {
        try {
            String userUuid = tokenUserService.getCurrentUserUuid();
            return ResponseEntity.ok("User role endpoint accessed successfully by user UUID: " + userUuid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error accessing user role endpoint: " + e.getMessage());
        }
    }

    @GetMapping("/test-admin-role")
    public ResponseEntity<String> testAdminRole() {
        try {
            String userUuid = tokenUserService.getCurrentUserUuid();
            boolean isAdmin = tokenUserService.isAdmin();
            return ResponseEntity.ok("Admin role endpoint accessed successfully by user UUID: " + userUuid + " (isAdmin: " + isAdmin + ")");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error accessing admin role endpoint: " + e.getMessage());
        }
    }
}