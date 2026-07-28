package com.msvanegasg.facturaelectronica.auditlambda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class AuditEventWriterHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void processesValidMessagesWithoutBatchFailures() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        AuditEventWriterHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", auditBody(UUID.randomUUID()))), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
        assertThat(repository.requests.get(0).action()).isEqualTo("CONFIRM_SALE");
    }

    @Test
    void returnsOnlyFailedMessageIdsWhenOneMessageCannotBeProcessed() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        AuditEventWriterHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(
                message("message-ok", auditBody(UUID.randomUUID())),
                message("message-bad", "{invalid-json")), null);

        assertThat(response.getBatchItemFailures())
                .extracting(SQSBatchResponse.BatchItemFailure::getItemIdentifier)
                .containsExactly("message-bad");
        assertThat(repository.requests).hasSize(1);
    }

    @Test
    void treatsInboxDuplicatesAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(false);
        AuditEventWriterHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-duplicate", auditBody(UUID.randomUUID()))),
                null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
    }

    private AuditEventWriterHandler handler(FakeRepository repository) {
        EventBridgeSqsEnvelopeParser parser = new EventBridgeSqsEnvelopeParser(objectMapper);
        ProcessAuditEventRequestedService service = new ProcessAuditEventRequestedService(
                new AuditEventRequestedMapper(objectMapper), repository);
        return new AuditEventWriterHandler(parser, service);
    }

    private SQSEvent event(SQSEvent.SQSMessage... messages) {
        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(messages));
        return event;
    }

    private SQSEvent.SQSMessage message(String id, String body) {
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId(id);
        message.setBody(body);
        return message;
    }

    private String auditBody(UUID eventId) throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        return objectMapper.writeValueAsString(Map.of(
                "eventId", eventId.toString(),
                "eventType", EventTypes.AUDIT_EVENT_REQUESTED,
                "eventVersion", 1,
                "occurredAt", Instant.parse("2026-07-21T10:15:30Z").toString(),
                "companyId", companyId.toString(),
                "aggregateType", "Sale",
                "aggregateId", saleId.toString(),
                "producer", "billing-service",
                "idempotencyKey", "sale-1:audit-requested",
                "payload", Map.of(
                        "companyId", companyId.toString(),
                        "eventType", "ELECTRONIC_DOCUMENT",
                        "resourceType", "SALE",
                        "resourceId", saleId.toString(),
                        "action", "CONFIRM_SALE",
                        "result", "SUCCESS",
                        "detail", "{\"saleId\":\"" + saleId + "\"}")));
    }

    private static final class FakeRepository implements AuditEventInboxRepositoryPort {
        private final boolean saveResult;
        private final List<AuditEventWriteRequest> requests = new ArrayList<>();

        private FakeRepository(boolean saveResult) {
            this.saveResult = saveResult;
        }

        @Override
        public boolean saveIfNew(DomainEventEnvelope envelope, AuditEventWriteRequest request) {
            requests.add(request);
            return saveResult;
        }
    }
}
