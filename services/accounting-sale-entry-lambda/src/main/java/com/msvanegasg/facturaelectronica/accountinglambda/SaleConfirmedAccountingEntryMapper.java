package com.msvanegasg.facturaelectronica.accountinglambda;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class SaleConfirmedAccountingEntryMapper {

    private final ObjectMapper objectMapper;

    public SaleConfirmedAccountingEntryMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public boolean supports(DomainEventEnvelope envelope) {
        return envelope != null && EventTypes.SALE_CONFIRMED.equals(envelope.eventType());
    }

    public AccountingSaleEntryRequest toRequest(DomainEventEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope is required");
        JsonNode payload = objectMapper.valueToTree(envelope.payload());
        UUID saleId = requiredUuid(payload, "saleId");
        return new AccountingSaleEntryRequest(
                envelope.eventId(),
                envelope.companyId(),
                saleId,
                entryDate(payload, envelope),
                "Venta facturada " + saleId,
                optionalUuid(payload, "customerId"),
                requiredDecimal(payload, "subtotal"),
                requiredDecimal(payload, "taxTotal"),
                requiredDecimal(payload, "total"));
    }

    private static LocalDate entryDate(JsonNode payload, DomainEventEnvelope envelope) {
        JsonNode issuedAt = payload.path("issuedAt");
        if (!issuedAt.isMissingNode() && !issuedAt.isNull() && !issuedAt.asText().isBlank()) {
            return Instant.parse(issuedAt.asText().trim()).atZone(ZoneOffset.UTC).toLocalDate();
        }
        return envelope.occurredAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static UUID requiredUuid(JsonNode node, String field) {
        return UUID.fromString(requiredText(node, field));
    }

    private static UUID optionalUuid(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        return UUID.fromString(value.asText().trim());
    }
    private static BigDecimal requiredDecimal(JsonNode node, String field) {
        return new BigDecimal(requiredText(node, field));
    }

    private static String requiredText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.asText().trim();
    }
}