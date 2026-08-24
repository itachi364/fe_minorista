package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class BffSecurityAuditClient {

    private final RestClient auditClient;

    public BffSecurityAuditClient(RestClient.Builder builder, BffProperties properties) {
        this(builder.clone().baseUrl(properties.auditUrl()).build());
    }

    BffSecurityAuditClient(RestClient auditClient) {
        this.auditClient = auditClient;
    }

    public static BffSecurityAuditClient noop() {
        return new BffSecurityAuditClient((RestClient) null);
    }

    public void audit(HttpServletRequest request, UUID userId, String eventType, String action, String result,
            String detail) {
        UUID companyId = parseUuid(request.getHeader("X-Company-Id"));
        if (auditClient == null || companyId == null) {
            return;
        }
        try {
            auditClient.post()
                    .uri("/api/v1/audit-events")
                    .header("X-Company-Id", companyId.toString())
                    .body(new AuditRequest(userId, eventType, "SECURITY", resourceId(request), action, result,
                            auditDetail(request, detail)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Auditoria de seguridad best-effort para no bloquear el flujo principal.
        }
    }

    private static String resourceId(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || path.isBlank()) {
            return "unknown";
        }
        return path.length() <= 120 ? path : path.substring(0, 120);
    }

    private static String auditDetail(HttpServletRequest request, String detail) {
        return "{\"method\":\"%s\",\"path\":\"%s\",\"correlationId\":\"%s\",\"detail\":\"%s\"}"
                .formatted(escape(request.getMethod()), escape(request.getRequestURI()),
                        escape(request.getHeader("X-Correlation-Id")), escape(detail));
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record AuditRequest(UUID userId, String eventType, String resourceType, String resourceId, String action,
            String result, String detail) {
    }
}
