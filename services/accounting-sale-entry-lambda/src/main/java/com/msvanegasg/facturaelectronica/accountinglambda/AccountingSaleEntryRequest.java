package com.msvanegasg.facturaelectronica.accountinglambda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AccountingSaleEntryRequest(UUID eventId, UUID companyId, UUID saleId, LocalDate entryDate,
        String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {

    public AccountingSaleEntryRequest {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(saleId, "saleId is required");
        Objects.requireNonNull(entryDate, "entryDate is required");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        description = description.trim();
        requireNonNegative(subtotal, "subtotal");
        requireNonNegative(taxTotal, "taxTotal");
        requireNonNegative(total, "total");
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
    }
}