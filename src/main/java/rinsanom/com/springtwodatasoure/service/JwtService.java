package rinsanom.com.springtwodatasoure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.base-secret:your-super-secret-key-change-this-in-production}")
    private String baseSecret;

    @Value("${app.jwt.expiration:604800000}") // 7 days in milliseconds
    private long jwtExpiration;

    public String generateToken(String userId, String projectId, String email) {
        String projectSecret = generateProjectSecret(projectId);
        SecretKey key = Keys.hmacShaKeyFor(projectSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userId)
                .claim("projectId", projectId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token, String projectId) {
        String projectSecret = generateProjectSecret(projectId);
        SecretKey key = Keys.hmacShaKeyFor(projectSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    private String generateProjectSecret(String projectId) {
        return baseSecret + "_" + projectId;
    }
}
