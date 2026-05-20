package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.Instant;
import java.util.UUID;

public record FiscalAuditEventResult(
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
