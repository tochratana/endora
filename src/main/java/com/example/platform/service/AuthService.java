package com.example.platform.service;

import com.example.platform.model.dto.LoginRequest;
import com.example.platform.model.dto.RegisterRequest;
import com.example.platform.model.dto.TokenResponse;
import com.example.platform.security.JwtService;
import com.example.platform.util.SchemaName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JdbcTemplate jdbc;
    private final JwtService jwtService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void register(UUID projectId, RegisterRequest req) {
        String schema = SchemaName.from(projectId);
        Long exists = jdbc.queryForObject("select count(*) from " + schema + ".users where username=? or email=?",
                Long.class, req.username(), req.email());
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("User already exists");
        }
        String sql = "insert into " + schema + ".users(username,email,password_hash,extra) values (?,?,?,to_json(?::json)) returning id";
        UUID userId = jdbc.queryForObject(sql, (rs, row) -> rs.getObject("id", UUID.class),
                req.username(), req.email(), encoder.encode(req.password()), toJson(req.extra()));
        jdbc.update("insert into " + schema + ".user_roles(user_id, role) values (?,?)", userId, "authenticated");
    }

    public TokenResponse login(UUID projectId, LoginRequest req) {
        String schema = SchemaName.from(projectId);
        Map<String, Object> user;
        try {
            user = jdbc.queryForMap("select id, password_hash from " + schema + ".users where username=? or email=?",
                    req.usernameOrEmail(), req.usernameOrEmail());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String hash = (String) user.get("password_hash");
        if (!encoder.matches(req.password(), hash)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        UUID userId = (UUID) user.get("id");
        List<String> roles = jdbc.query("select role from " + schema + ".user_roles where user_id=?",
                (rs, row) -> rs.getString("role"), userId);
        String access = jwtService.generateAccessToken(userId, projectId, roles);
        String refresh = jwtService.generateRefreshToken(userId, projectId, roles);
        return new TokenResponse(access, refresh);
    }

    public TokenResponse refresh(String refreshToken) {
        Jwt jwt = jwtService.decode(refreshToken);
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID projectId = UUID.fromString(jwt.getClaimAsString("project_id"));
        List<String> roles = jwt.getClaimAsStringList("roles");
        String access = jwtService.generateAccessToken(userId, projectId, roles);
        String newRefresh = jwtService.generateRefreshToken(userId, projectId, roles);
        return new TokenResponse(access, newRefresh);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map == null ? Map.of() : map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
