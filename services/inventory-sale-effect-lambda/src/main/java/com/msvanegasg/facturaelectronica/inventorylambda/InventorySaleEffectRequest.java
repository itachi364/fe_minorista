package com.msvanegasg.facturaelectronica.inventorylambda;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record InventorySaleEffectRequest(UUID eventId, UUID companyId, UUID saleId, String documentIdempotencyKey,
        Instant occurredAt, List<InventorySaleLineEffect> lines) {

    public InventorySaleEffectRequest {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(saleId, "saleId is required");
        if (documentIdempotencyKey == null || documentIdempotencyKey.isBlank()) {
            throw new IllegalArgumentException("documentIdempotencyKey is required");
        }
        documentIdempotencyKey = documentIdempotencyKey.trim();
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines are required"));
    }

    public List<InventorySaleLineEffect> stockTrackedLines() {
        return lines.stream().filter(InventorySaleLineEffect::stockTracked).toList();
    }
}
