package com.msvanegasg.facturaelectronica.audit.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(UUID id, UUID companyId, UUID userId, String eventType, String resourceType,
        String resourceId, String action, AuditResult result, String detail, Instant occurredAt) {

    public AuditEvent {
        require(id, "id");
        require(companyId, "companyId");
        eventType = requiredText(eventType, "eventType");
        resourceType = requiredText(resourceType, "resourceType");
        action = requiredText(action, "action");
        require(result, "result");
        require(occurredAt, "occurredAt");
        resourceId = optionalText(resourceId);
        detail = optionalText(detail);
    }

    public static AuditEvent register(UUID id, UUID companyId, UUID userId, String eventType, String resourceType,
            String resourceId, String action, AuditResult result, String detail, Instant occurredAt) {
        return new AuditEvent(id, companyId, userId, eventType, resourceType, resourceId, action, result, detail,
                occurredAt);
    }

    private static void require(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }

    private static String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
