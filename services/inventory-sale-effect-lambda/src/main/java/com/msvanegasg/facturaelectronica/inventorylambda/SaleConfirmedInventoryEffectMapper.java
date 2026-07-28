package com.msvanegasg.facturaelectronica.inventorylambda;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class SaleConfirmedInventoryEffectMapper {

    private final ObjectMapper objectMapper;

    public SaleConfirmedInventoryEffectMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public boolean supports(DomainEventEnvelope envelope) {
        return envelope != null && EventTypes.SALE_CONFIRMED.equals(envelope.eventType());
    }

    public InventorySaleEffectRequest toRequest(DomainEventEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope is required");
        JsonNode payload = objectMapper.valueToTree(envelope.payload());
        return new InventorySaleEffectRequest(
                envelope.eventId(),
                envelope.companyId(),
                requiredUuid(payload, "saleId"),
                requiredText(payload, "documentIdempotencyKey"),
                envelope.occurredAt(),
                lines(payload.path("lines")));
    }

    private List<InventorySaleLineEffect> lines(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("lines are required");
        }
        List<InventorySaleLineEffect> lines = new ArrayList<>();
        node.forEach(line -> lines.add(new InventorySaleLineEffect(
                requiredUuid(line, "lineId"),
                requiredUuid(line, "productId"),
                requiredDecimal(line, "quantity"),
                optionalDecimal(line, "unitCost", BigDecimal.ZERO),
                line.path("stockTracked").asBoolean(false))));
        return lines;
    }

    private static UUID requiredUuid(JsonNode node, String field) {
        return UUID.fromString(requiredText(node, field));
    }

    private static BigDecimal requiredDecimal(JsonNode node, String field) {
        return new BigDecimal(requiredText(node, field));
    }

    private static BigDecimal optionalDecimal(JsonNode node, String field, BigDecimal fallback) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return fallback;
        }
        return new BigDecimal(value.asText());
    }

    private static String requiredText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.asText().trim();
    }
}
