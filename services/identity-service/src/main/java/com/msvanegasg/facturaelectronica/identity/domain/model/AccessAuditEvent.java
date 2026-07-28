package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AccessAuditEvent(
        UUID id,
        UUID companyId,
        UUID userId,
        String action,
        String resourceType,
        String resourceId,
        AccessAuditResult result,
        String detail,
        Instant occurredAt) {

    public AccessAuditEvent {
        Objects.requireNonNull(id, "id is required");
        action = required(action, "action");
        resourceType = required(resourceType, "resourceType");
        Objects.requireNonNull(result, "result is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public static AccessAuditEvent register(UUID id, UUID companyId, UUID userId, String action, String resourceType,
            String resourceId, AccessAuditResult result, String detail, Instant occurredAt) {
        return new AccessAuditEvent(id, companyId, userId, action, resourceType, resourceId, result, detail,
                occurredAt);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
