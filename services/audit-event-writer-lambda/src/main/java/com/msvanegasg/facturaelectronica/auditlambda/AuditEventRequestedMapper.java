package com.msvanegasg.facturaelectronica.auditlambda;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class AuditEventRequestedMapper {

    private final ObjectMapper objectMapper;

    public AuditEventRequestedMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public AuditEventWriteRequest toRequest(DomainEventEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope is required");
        JsonNode payload = objectMapper.valueToTree(envelope.payload());
        UUID companyId = optionalUuid(payload, "companyId", envelope.companyId());
        return new AuditEventWriteRequest(
                envelope.eventId(),
                companyId,
                optionalUuid(payload, "userId", null),
                requiredText(payload, "eventType"),
                requiredText(payload, "resourceType"),
                optionalText(payload, "resourceId"),
                requiredText(payload, "action"),
                requiredText(payload, "result"),
                normalizeDetail(payload.path("detail")),
                envelope.occurredAt());
    }

    public boolean supports(DomainEventEnvelope envelope) {
        return envelope != null && EventTypes.AUDIT_EVENT_REQUESTED.equals(envelope.eventType());
    }

    private UUID optionalUuid(JsonNode node, String field, UUID fallback) {
        String value = optionalText(node, field);
        return value == null ? fallback : UUID.fromString(value);
    }

    private static String requiredText(JsonNode node, String field) {
        String value = optionalText(node, field);
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String optionalText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private String normalizeDetail(JsonNode detail) {
        if (detail == null || detail.isMissingNode() || detail.isNull()) {
            return "{}";
        }
        if (!detail.isTextual()) {
            return detail.toString();
        }
        String raw = detail.asText();
        if (raw == null || raw.isBlank()) {
            return "{}";
        }
        try {
            objectMapper.readTree(raw);
            return raw.trim();
        } catch (JsonProcessingException ignored) {
            try {
                return objectMapper.writeValueAsString(java.util.Map.of("message", raw));
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("detail cannot be serialized", exception);
            }
        }
    }
}
