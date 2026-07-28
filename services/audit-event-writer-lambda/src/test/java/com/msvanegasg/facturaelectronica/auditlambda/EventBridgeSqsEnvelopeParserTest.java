package com.msvanegasg.facturaelectronica.auditlambda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class EventBridgeSqsEnvelopeParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventBridgeSqsEnvelopeParser parser = new EventBridgeSqsEnvelopeParser(objectMapper);

    @Test
    void parsesEventBridgeMessageBody() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        String body = objectMapper.writeValueAsString(Map.of(
                "version", "0",
                "id", UUID.randomUUID().toString(),
                "detail-type", EventTypes.AUDIT_EVENT_REQUESTED,
                "source", "billing-service",
                "detail", Map.of(
                        "eventId", eventId.toString(),
                        "eventType", EventTypes.AUDIT_EVENT_REQUESTED,
                        "eventVersion", 1,
                        "occurredAt", "2026-07-21T10:15:30Z",
                        "companyId", companyId.toString(),
                        "aggregateType", "Sale",
                        "aggregateId", saleId.toString(),
                        "producer", "billing-service",
                        "idempotencyKey", "sale-1:audit-requested",
                        "payload", Map.of(
                                "eventType", "ELECTRONIC_DOCUMENT",
                                "resourceType", "SALE",
                                "resourceId", saleId.toString(),
                                "action", "CONFIRM_SALE",
                                "result", "SUCCESS",
                                "detail", "{}"))));

        DomainEventEnvelope envelope = parser.parse(body);

        assertThat(envelope.eventId()).isEqualTo(eventId);
        assertThat(envelope.eventType()).isEqualTo(EventTypes.AUDIT_EVENT_REQUESTED);
        assertThat(envelope.companyId()).isEqualTo(companyId);
        assertThat(envelope.aggregateId()).isEqualTo(saleId);
        assertThat(envelope.occurredAt()).isEqualTo(Instant.parse("2026-07-21T10:15:30Z"));
        assertThat(envelope.payload()).containsEntry("action", "CONFIRM_SALE");
    }
}
