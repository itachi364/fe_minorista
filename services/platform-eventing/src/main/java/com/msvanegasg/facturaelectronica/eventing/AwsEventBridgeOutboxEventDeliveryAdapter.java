package com.msvanegasg.facturaelectronica.eventing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

public class AwsEventBridgeOutboxEventDeliveryAdapter implements OutboxEventDeliveryPort {

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;
    private final String eventBusName;

    public AwsEventBridgeOutboxEventDeliveryAdapter(EventBridgeClient eventBridgeClient, ObjectMapper objectMapper,
            String eventBusName) {
        this.eventBridgeClient = Objects.requireNonNull(eventBridgeClient, "eventBridgeClient is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
        this.eventBusName = Objects.requireNonNull(eventBusName, "eventBusName is required");
    }

    @Override
    public void deliver(OutboxEventRecord event) {
        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName(eventBusName)
                .source(event.producer())
                .detailType(event.eventType())
                .detail(toDetailJson(event))
                .build();
        PutEventsResponse response = eventBridgeClient.putEvents(PutEventsRequest.builder().entries(entry).build());
        if (response.failedEntryCount() != null && response.failedEntryCount() > 0) {
            String message = response.entries().isEmpty() ? "EventBridge rejected event"
                    : response.entries().get(0).errorMessage();
            throw new EventDeliveryException(message == null ? "EventBridge rejected event" : message);
        }
    }

    private String toDetailJson(OutboxEventRecord event) {
        try {
            JsonNode payload = objectMapper.readTree(event.payloadJson());
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("eventId", event.eventId());
            detail.put("eventType", event.eventType());
            detail.put("eventVersion", event.eventVersion());
            detail.put("occurredAt", event.occurredAt());
            detail.put("companyId", event.companyId());
            detail.put("aggregateType", event.aggregateType());
            detail.put("aggregateId", event.aggregateId());
            detail.put("producer", event.producer());
            detail.put("correlationId", event.correlationId());
            detail.put("idempotencyKey", event.idempotencyKey());
            detail.put("payload", payload);
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException exception) {
            throw new EventDeliveryException("Invalid outbox payload JSON", exception);
        }
    }

    public static class EventDeliveryException extends RuntimeException {
        public EventDeliveryException(String message) {
            super(message);
        }

        public EventDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}