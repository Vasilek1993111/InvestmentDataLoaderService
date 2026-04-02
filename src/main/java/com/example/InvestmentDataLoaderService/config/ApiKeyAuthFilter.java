package com.example.InvestmentDataLoaderService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lightweight API key protection for non-test environments.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/error"
    );

    private final boolean enabled;
    private final String headerName;
    private final String expectedApiKey;

    public ApiKeyAuthFilter(boolean enabled, String headerName, String expectedApiKey) {
        this.enabled = enabled;
        this.headerName = (headerName == null || headerName.isBlank()) ? API_KEY_HEADER : headerName.trim();
        this.expectedApiKey = expectedApiKey == null ? "" : expectedApiKey.trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !enabled || PUBLIC_PATHS.stream().anyMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (expectedApiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                    {"success":false,"error":"ServiceUnavailable","message":"API key is not configured"}
                    """.trim());
            return;
        }

        String providedApiKey = request.getHeader(headerName);
        if (providedApiKey == null || !expectedApiKey.equals(providedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey");
            response.getWriter().write("""
                    {"success":false,"error":"Unauthorized","message":"Missing or invalid API key"}
                    """.trim());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
