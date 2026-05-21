package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AccountsPayablePayment(
        UUID id,
        UUID companyId,
        UUID accountsPayableId,
        LocalDate paymentDate,
        BigDecimal amount,
        String paymentMethod,
        String reference,
        UUID createdBy,
        Instant createdAt) {

    public AccountsPayablePayment {
        require(id, "id");
        require(companyId, "companyId");
        require(accountsPayableId, "accountsPayableId");
        require(paymentDate, "paymentDate");
        requireMoney(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        paymentMethod = normalizeRequired(paymentMethod, 80, "paymentMethod");
        reference = normalizeOptional(reference, 120, "reference");
        require(createdAt, "createdAt");
    }

    private static void requireMoney(BigDecimal value, String field) {
        require(value, field);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be zero or positive");
        }
    }

    private static String normalizeRequired(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalizeOptional(value, maxLength, field);
    }

    private static String normalizeOptional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " length is invalid");
        }
        return normalized;
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
