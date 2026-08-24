package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BffCsrfFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    public static final String CSRF_COOKIE = "NF_CSRF";
    public static final String CSRF_HEADER = "X-CSRF-Token";

    private final BffAuthProperties properties;

    public BffCsrfFilter(BffAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isEnabled() || !MUTATING_METHODS.contains(request.getMethod()) || isAuthBootstrap(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String cookieToken = cookieValue(request, CSRF_COOKIE);
        String headerToken = request.getHeader(CSRF_HEADER);
        if (cookieToken == null || headerToken == null || !cookieToken.equals(headerToken)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"CSRF_VALIDATION_ERROR\",\"message\":\"Token CSRF invalido.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isEnabled() {
        return properties.csrfEnabled() || properties.isCognitoMode() || properties.isProductionEnvironment();
    }

    private static boolean isAuthBootstrap(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/logout") || path.equals("/api/v1/auth/callback");
    }

    private static String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
