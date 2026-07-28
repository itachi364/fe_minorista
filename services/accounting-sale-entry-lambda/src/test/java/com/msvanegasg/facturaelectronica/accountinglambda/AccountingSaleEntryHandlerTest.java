package com.msvanegasg.facturaelectronica.accountinglambda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class AccountingSaleEntryHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void processesSaleConfirmedMessagesWithoutBatchFailures() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        AccountingSaleEntryHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", saleConfirmedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
        assertThat(repository.requests.get(0).subtotal()).isEqualByComparingTo("100.00");
        assertThat(repository.requests.get(0).taxTotal()).isEqualByComparingTo("19.00");
        assertThat(repository.requests.get(0).total()).isEqualByComparingTo("119.00");
    }

    @Test
    void ignoresUnsupportedEventsAsSuccessfulMessages() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        AccountingSaleEntryHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-1", unsupportedBody())), null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).isEmpty();
    }

    @Test
    void returnsOnlyFailedMessageIdsWhenOneMessageCannotBeProcessed() throws Exception {
        FakeRepository repository = new FakeRepository(true);
        AccountingSaleEntryHandler handler = handler(repository);

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
        AccountingSaleEntryHandler handler = handler(repository);

        SQSBatchResponse response = handler.handleRequest(event(message("message-duplicate", saleConfirmedBody())),
                null);

        assertThat(response.getBatchItemFailures()).isEmpty();
        assertThat(repository.requests).hasSize(1);
    }

    private AccountingSaleEntryHandler handler(FakeRepository repository) {
        EventBridgeSqsEnvelopeParser parser = new EventBridgeSqsEnvelopeParser(objectMapper);
        ProcessAccountingSaleEntryService service = new ProcessAccountingSaleEntryService(
                new SaleConfirmedAccountingEntryMapper(objectMapper), repository);
        return new AccountingSaleEntryHandler(parser, service);
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
        return body(EventTypes.SALE_CONFIRMED, saleId, Map.of(
                "saleId", saleId.toString(),
                "documentId", UUID.randomUUID().toString(),
                "documentIdempotencyKey", "confirm-1",
                "documentStatus", "VALIDATED",
                "issuedAt", "2026-07-21T10:15:30Z",
                "customerId", UUID.randomUUID().toString(),
                "subtotal", "100.00",
                "taxTotal", "19.00",
                "total", "119.00"));
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

    private static final class FakeRepository implements AccountingSaleEntryRepositoryPort {
        private final boolean saveResult;
        private final java.util.ArrayList<AccountingSaleEntryRequest> requests = new java.util.ArrayList<>();

        private FakeRepository(boolean saveResult) {
            this.saveResult = saveResult;
        }

        @Override
        public boolean applyIfNew(DomainEventEnvelope envelope, AccountingSaleEntryRequest request) {
            requests.add(request);
            return saveResult;
        }
    }
}