package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.UUID;

public record FiscalAuditEvent(
        UUID id,
        UUID companyId,
        UUID resourceId,
        String resourceType,
        String action,
        String result,
        UUID userId,
        Instant occurredAt,
        String detail) {
}
