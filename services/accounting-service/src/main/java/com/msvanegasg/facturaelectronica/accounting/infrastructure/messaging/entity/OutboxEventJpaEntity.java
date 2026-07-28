package com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.OutboxEventRecord;

@Entity
@Table(name = "accounting_outbox_event")
public class OutboxEventJpaEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;
    @Column(name = "event_version", nullable = false)
    private int eventVersion;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "aggregate_type", nullable = false, length = 120)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(nullable = false, length = 120)
    private String producer;
    @Column(name = "correlation_id", length = 120)
    private String correlationId;
    @Column(name = "idempotency_key", nullable = false, length = 180)
    private String idempotencyKey;
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "publish_attempts", nullable = false)
    private int publishAttempts;
    @Column(name = "last_error")
    private String lastError;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static OutboxEventJpaEntity from(DomainEventEnvelope event, String payloadJson) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.eventId = event.eventId();
        entity.eventType = event.eventType();
        entity.eventVersion = event.eventVersion();
        entity.occurredAt = event.occurredAt();
        entity.companyId = event.companyId();
        entity.aggregateType = event.aggregateType();
        entity.aggregateId = event.aggregateId();
        entity.producer = event.producer();
        entity.correlationId = event.correlationId();
        entity.idempotencyKey = event.idempotencyKey();
        entity.payloadJson = payloadJson;
        entity.status = "PENDING";
        entity.publishAttempts = 0;
        entity.createdAt = event.occurredAt();
        return entity;
    }


    public OutboxEventRecord toRecord() {
        return new OutboxEventRecord(eventId, eventType, eventVersion, occurredAt, companyId, aggregateType, aggregateId,
                producer, correlationId, idempotencyKey, payloadJson);
    }

    public void markPublished(Instant publishedAt) {
        this.status = "PUBLISHED";
        this.publishAttempts++;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.publishAttempts++;
        this.lastError = errorMessage == null ? null : errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
    }
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public int getEventVersion() { return eventVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public UUID getCompanyId() { return companyId; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getProducer() { return producer; }
    public String getCorrelationId() { return correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getPayloadJson() { return payloadJson; }
    public String getStatus() { return status; }
    public int getPublishAttempts() { return publishAttempts; }
    public String getLastError() { return lastError; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
