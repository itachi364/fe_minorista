package com.msvanegasg.facturaelectronica.audit.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;

public record AuditEventResult(UUID id, UUID companyId, UUID userId, String eventType, String resourceType,
        String resourceId, String action, AuditResult result, String detail, Instant occurredAt) {
}
