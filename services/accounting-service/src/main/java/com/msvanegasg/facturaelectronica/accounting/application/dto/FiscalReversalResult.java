package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.Instant;
import java.util.UUID;

public record FiscalReversalResult(UUID id, UUID companyId, UUID calculationId, String reason,
        UUID reversedBy, Instant reversedAt, UUID compensatingEntryId) {
    public FiscalReversalResult(UUID id, UUID companyId, UUID calculationId, String reason,
            UUID reversedBy, Instant reversedAt) {
        this(id, companyId, calculationId, reason, reversedBy, reversedAt, null);
    }
}
