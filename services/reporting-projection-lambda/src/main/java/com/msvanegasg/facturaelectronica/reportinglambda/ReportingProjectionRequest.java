package com.msvanegasg.facturaelectronica.reportinglambda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReportingProjectionRequest(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID companyId,
        String aggregateType,
        UUID aggregateId,
        String producer,
        String correlationId,
        String idempotencyKey,
        UUID saleId,
        UUID documentId,
        UUID movementId,
        UUID accountingEntryId,
        UUID productId,
        String status,
        BigDecimal amount,
        String payloadJson) {
}