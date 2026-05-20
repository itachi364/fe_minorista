package com.msvanegasg.facturaelectronica.audit.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditEventQuery(UUID companyId, String resourceType, String resourceId, Instant from, Instant to,
        UUID userId) {
}
