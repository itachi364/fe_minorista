package com.msvanegasg.facturaelectronica.auditlambda;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEventWriteRequest(UUID eventId, UUID companyId, UUID userId, String eventType, String resourceType,
        String resourceId, String action, String result, String detail, Instant occurredAt) {

    public AuditEventWriteRequest {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(companyId, "companyId is required");
        eventType = requiredText(eventType, "eventType");
        resourceType = requiredText(resourceType, "resourceType");
        action = requiredText(action, "action");
        result = requiredText(result, "result");
        detail = detail == null || detail.isBlank() ? "{}" : detail.trim();
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    private static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
