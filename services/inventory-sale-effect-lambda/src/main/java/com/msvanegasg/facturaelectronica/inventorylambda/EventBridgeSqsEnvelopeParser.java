package com.msvanegasg.facturaelectronica.inventorylambda;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class EventBridgeSqsEnvelopeParser {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public EventBridgeSqsEnvelopeParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public DomainEventEnvelope parse(String messageBody) {
        if (messageBody == null || messageBody.isBlank()) {
            throw new IllegalArgumentException("SQS message body is required");
        }
        try {
            JsonNode root = objectMapper.readTree(messageBody);
            JsonNode detail = isEventBridgeEnvelope(root) ? root.path("detail") : root;
            return new DomainEventEnvelope(
                    uuid(detail, "eventId"),
                    text(detail, "eventType"),
                    integer(detail, "eventVersion"),
                    instant(detail, "occurredAt"),
                    uuid(detail, "companyId"),
                    text(detail, "aggregateType"),
                    uuid(detail, "aggregateId"),
                    text(detail, "producer"),
                    nullableText(detail, "correlationId"),
                    text(detail, "idempotencyKey"),
                    payload(detail.path("payload")));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("SQS message body is not valid JSON", exception);
        }
    }

    private boolean isEventBridgeEnvelope(JsonNode root) {
        return root.has("detail-type") && root.has("source") && root.has("detail");
    }

    private Map<String, Object> payload(JsonNode payload) {
        if (payload == null || payload.isMissingNode() || payload.isNull()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(objectMapper.convertValue(payload, MAP_TYPE));
    }

    private static UUID uuid(JsonNode node, String field) {
        return UUID.fromString(text(node, field));
    }

    private static Instant instant(JsonNode node, String field) {
        return Instant.parse(text(node, field));
    }

    private static int integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.canConvertToInt()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.asInt();
    }

    private static String text(JsonNode node, String field) {
        String value = nullableText(node, field);
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String nullableText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }
}
