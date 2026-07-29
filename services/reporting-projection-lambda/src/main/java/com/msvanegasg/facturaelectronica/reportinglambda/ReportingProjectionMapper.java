package com.msvanegasg.facturaelectronica.reportinglambda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class ReportingProjectionMapper {

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            EventTypes.SALE_CONFIRMED,
            EventTypes.ELECTRONIC_DOCUMENT_VALIDATED,
            EventTypes.INVENTORY_MOVEMENT_REGISTERED,
            EventTypes.ACCOUNTING_ENTRY_POSTED);

    private final ObjectMapper objectMapper;

    public ReportingProjectionMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public boolean supports(DomainEventEnvelope envelope) {
        return envelope != null && SUPPORTED_EVENTS.contains(envelope.eventType());
    }

    public ReportingProjectionRequest toRequest(DomainEventEnvelope envelope) {
        Map<String, Object> payload = envelope.payload();
        return new ReportingProjectionRequest(
                envelope.eventId(),
                envelope.eventType(),
                envelope.eventVersion(),
                envelope.occurredAt(),
                envelope.companyId(),
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.producer(),
                envelope.correlationId(),
                envelope.idempotencyKey(),
                uuid(payload, "saleId"),
                uuid(payload, "documentId"),
                uuid(payload, "movementId"),
                uuid(payload, "entryId"),
                uuid(payload, "productId"),
                status(envelope.eventType(), payload),
                amount(envelope.eventType(), payload),
                payloadJson(payload));
    }

    private String status(String eventType, Map<String, Object> payload) {
        return switch (eventType) {
            case EventTypes.SALE_CONFIRMED -> firstText(payload, "saleStatus", "documentStatus");
            case EventTypes.ELECTRONIC_DOCUMENT_VALIDATED -> firstText(payload, "documentStatus", "providerStatus");
            case EventTypes.INVENTORY_MOVEMENT_REGISTERED -> text(payload, "movementType");
            case EventTypes.ACCOUNTING_ENTRY_POSTED -> text(payload, "status");
            default -> null;
        };
    }

    private BigDecimal amount(String eventType, Map<String, Object> payload) {
        BigDecimal value = switch (eventType) {
            case EventTypes.SALE_CONFIRMED, EventTypes.ELECTRONIC_DOCUMENT_VALIDATED -> decimal(payload, "total");
            case EventTypes.INVENTORY_MOVEMENT_REGISTERED -> decimal(payload, "quantity")
                    .multiply(decimal(payload, "unitCost"));
            case EventTypes.ACCOUNTING_ENTRY_POSTED -> decimal(payload, "debitTotal");
            default -> BigDecimal.ZERO;
        };
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String payloadJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("event payload could not be serialized", exception);
        }
    }

    private static UUID uuid(Map<String, Object> payload, String key) {
        String value = text(payload, key);
        return value == null ? null : UUID.fromString(value);
    }

    private static BigDecimal decimal(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private static String firstText(Map<String, Object> payload, String primary, String fallback) {
        String value = text(payload, primary);
        return value == null ? text(payload, fallback) : value;
    }

    private static String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text == null || text.isBlank() ? null : text.trim();
    }
}