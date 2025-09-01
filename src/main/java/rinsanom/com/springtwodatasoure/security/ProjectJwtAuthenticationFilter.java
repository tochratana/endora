package rinsanom.com.springtwodatasoure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rinsanom.com.springtwodatasoure.service.JwtService;
import rinsanom.com.springtwodatasoure.service.TableService;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectJwtAuthenticationFilter implements Filter {

    private final JwtService jwtService;
    private final TableService tableService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Skip auth endpoints
        if (path.contains("/auth/") ||
                !path.startsWith("/client-api/")) { // Only apply to client-api paths
            chain.doFilter(request, response);
            return;
        }

        // Extract projectId from path pattern /client-api/{projectId}/...
        Pattern pattern = Pattern.compile("/client-api/([^/]+)/");
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            String projectId = matcher.group(1);

            // Check if project has users table (authentication enabled)
            boolean hasUsersTable = tableService.getTableByNameAndProject("users", projectId) != null ||
                    tableService.getTableByNameAndProject("user", projectId) != null;

            if (hasUsersTable) {
                // Require authentication
                String authHeader = httpRequest.getHeader("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    httpResponse.setStatus(401);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Authorization header required\"}");
                    return;
                }

                try {
                    String token = authHeader.substring(7);
                    Claims claims = jwtService.validateToken(token, projectId);

                    // Add user info to request attributes
                    httpRequest.setAttribute("user", Map.of(
                            "id", claims.getSubject(),
                            "projectId", claims.get("projectId"),
                            "email", claims.get("email")
                    ));

                } catch (Exception e) {
                    log.warn("JWT validation failed for project {}: {}", projectId, e.getMessage());
                    httpResponse.setStatus(401);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}