package com.msvanegasg.facturaelectronica.reportinglambda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class ReportingProjectionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void processesSupportedMessagesWithoutBatchFailures() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        ReportingProjectionHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", saleConfirmedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
        assertThat(repository.requests.get(0).eventType()).isEqualTo(EventTypes.SALE_CONFIRMED);
        assertThat(repository.requests.get(0).amount()).isEqualByComparingTo("1190.00");
    }

    @Test
    void ignoresUnsupportedEventsAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        ReportingProjectionHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", unsupportedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).isEmpty();
    }

    @Test
    void returnsOnlyFailedMessageIdsWhenOneMessageCannotBeProcessed() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        ReportingProjectionHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(
                message("message-ok", inventoryMovementBody()),
                message("message-bad", "{invalid-json")), null);

        assertThat(response.getBatchItemFailures())
                .extracting(SQSBatchResponse.BatchItemFailure::getItemIdentifier)
                .containsExactly("message-bad");
        assertThat(repository.requests).hasSize(1);
        assertThat(repository.requests.get(0).amount()).isEqualByComparingTo("18000.00");
    }

    @Test
    void treatsInboxDuplicatesAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(false);
        ReportingProjectionHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-duplicate", saleConfirmedBody())),
                null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
    }

    private ReportingProjectionHandler handler(FakeRepository repository) {
        EventBridgeSqsEnvelopeParser parser = new EventBridgeSqsEnvelopeParser(objectMapper);
        ProcessReportingProjectionService service = new ProcessReportingProjectionService(
                new ReportingProjectionMapper(objectMapper), repository);
        return new ReportingProjectionHandler(parser, service);
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

    private String saleConfirmedBody() throws Exception {
        UUID saleId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        return body(EventTypes.SALE_CONFIRMED, saleId, Map.of(
                "saleId", saleId.toString(),
                "documentId", documentId.toString(),
                "saleStatus", "CONFIRMED",
                "documentStatus", "VALIDATED",
                "total", "1190.00"));
    }

    private String inventoryMovementBody() throws Exception {
        UUID movementId = UUID.randomUUID();
        return body(EventTypes.INVENTORY_MOVEMENT_REGISTERED, movementId, Map.of(
                "movementId", movementId.toString(),
                "productId", UUID.randomUUID().toString(),
                "movementType", "SALE_OUT",
                "quantity", "2.00",
                "unitCost", "9000.00"));
    }

    private String unsupportedBody() throws Exception {
        return body(EventTypes.AUDIT_EVENT_REQUESTED, UUID.randomUUID(), Map.of("resourceType", "SALE"));
    }

    private String body(String eventType, UUID aggregateId, Map<String, Object> payload) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "eventVersion", 1,
                "occurredAt", Instant.parse("2026-07-21T10:15:30Z").toString(),
                "companyId", UUID.randomUUID().toString(),
                "aggregateType", "Sale",
                "aggregateId", aggregateId.toString(),
                "producer", "billing-service",
                "idempotencyKey", "projection-key",
                "payload", payload));
    }

    private static final class FakeRepository implements ReportingProjectionRepositoryPort {
        private final boolean saveResult;
        private final ArrayList<ReportingProjectionRequest> requests = new ArrayList<>();

        private FakeRepository(boolean saveResult) {
            this.saveResult = saveResult;
        }

        @Override
        public boolean projectIfNew(ReportingProjectionRequest request) {
            requests.add(request);
            return saveResult;
        }
    }
}