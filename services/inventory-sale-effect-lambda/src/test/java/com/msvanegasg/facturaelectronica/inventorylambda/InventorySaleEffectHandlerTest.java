package com.msvanegasg.facturaelectronica.inventorylambda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class InventorySaleEffectHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void processesSaleConfirmedMessagesWithoutBatchFailures() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        InventorySaleEffectHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", saleConfirmedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
        assertThat(repository.requests.get(0).stockTrackedLines()).hasSize(1);
    }

    @Test
    void ignoresUnsupportedEventsAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        InventorySaleEffectHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", unsupportedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).isEmpty();
    }

    @Test
    void returnsOnlyFailedMessageIdsWhenOneMessageCannotBeProcessed() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        InventorySaleEffectHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(
                message("message-ok", saleConfirmedBody()),
                message("message-bad", "{invalid-json")), null);

        assertThat(response.getBatchItemFailures())
                .extracting(SQSBatchResponse.BatchItemFailure::getItemIdentifier)
                .containsExactly("message-bad");
        assertThat(repository.requests).hasSize(1);
    }

    @Test
    void treatsInboxDuplicatesAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(false);
        InventorySaleEffectHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-duplicate", saleConfirmedBody())),
                null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
    }

    private InventorySaleEffectHandler handler(FakeRepository repository) {
        EventBridgeSqsEnvelopeParser parser = new EventBridgeSqsEnvelopeParser(objectMapper);
        ProcessInventorySaleEffectService service = new ProcessInventorySaleEffectService(
                new SaleConfirmedInventoryEffectMapper(objectMapper), repository);
        return new InventorySaleEffectHandler(parser, service);
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
        UUID productId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        return body(EventTypes.SALE_CONFIRMED, saleId, Map.of(
                "saleId", saleId.toString(),
                "documentId", UUID.randomUUID().toString(),
                "documentIdempotencyKey", "confirm-1",
                "documentStatus", "VALIDATED",
                "lines", List.of(Map.of(
                        "lineId", lineId.toString(),
                        "productId", productId.toString(),
                        "stockTracked", true,
                        "quantity", "2.00",
                        "unitCost", "9000.00"),
                        Map.of(
                                "lineId", UUID.randomUUID().toString(),
                                "productId", UUID.randomUUID().toString(),
                                "stockTracked", false,
                                "quantity", "1.00",
                                "unitCost", "0"))));
    }

    private String unsupportedBody() throws Exception {
        return body(EventTypes.ELECTRONIC_DOCUMENT_VALIDATED, UUID.randomUUID(), Map.of(
                "saleId", UUID.randomUUID().toString(),
                "documentId", UUID.randomUUID().toString()));
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
                "idempotencyKey", "confirm-1:sale-confirmed",
                "payload", payload));
    }

    private static final class FakeRepository implements InventorySaleEffectRepositoryPort {
        private final boolean saveResult;
        private final java.util.ArrayList<InventorySaleEffectRequest> requests = new java.util.ArrayList<>();

        private FakeRepository(boolean saveResult) {
            this.saveResult = saveResult;
        }

        @Override
        public boolean applyIfNew(DomainEventEnvelope envelope, InventorySaleEffectRequest request) {
            requests.add(request);
            return saveResult;
        }
    }
}
