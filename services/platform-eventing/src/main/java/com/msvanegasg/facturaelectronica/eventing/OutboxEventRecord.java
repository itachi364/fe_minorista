package com.msvanegasg.facturaelectronica.eventing;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OutboxEventRecord(
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
        String payloadJson) {

    public OutboxEventRecord {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(aggregateType, "aggregateType is required");
        Objects.requireNonNull(aggregateId, "aggregateId is required");
        Objects.requireNonNull(producer, "producer is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(payloadJson, "payloadJson is required");
        if (eventVersion < 1) {
            throw new IllegalArgumentException("eventVersion must be greater than zero");
        }
    }
}