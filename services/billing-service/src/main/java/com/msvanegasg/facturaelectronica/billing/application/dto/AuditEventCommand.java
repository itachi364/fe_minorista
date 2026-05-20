package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

public record AuditEventCommand(UUID companyId, UUID userId, String eventType, String resourceType, String resourceId,
        String action, AuditResult result, String detail) {
}
