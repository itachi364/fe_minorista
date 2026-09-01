package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Expense(
        UUID id,
        UUID companyId,
        UUID supplierId,
        ExpenseType expenseType,
        LocalDate expenseDate,
        String concept,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        PaymentCondition paymentCondition,
        LocalDate dueDate,
        String evidenceUrl,
        ExpenseStatus status,
        String idempotencyKey,
        Instant createdAt,
        Instant confirmedAt) {

    public Expense {
        require(id, "id");
        require(companyId, "companyId");
        expenseType = expenseType == null ? ExpenseType.OPERATING_EXPENSE : expenseType;
        require(expenseDate, "expenseDate");
        concept = normalizeRequired(concept, 250, "concept");
        requireMoney(subtotal, "subtotal");
        requireMoney(taxTotal, "taxTotal");
        requireMoney(total, "total");
        require(paymentCondition, "paymentCondition");
        if (paymentCondition == PaymentCondition.CREDIT && dueDate == null) {
            throw new IllegalArgumentException("dueDate is required for credit expenses");
        }
        evidenceUrl = normalizeOptional(evidenceUrl, 500, "evidenceUrl");
        require(status, "status");
        idempotencyKey = normalizeRequired(idempotencyKey, 120, "idempotencyKey");
        require(createdAt, "createdAt");
    }

    public static Expense pending(UUID id, UUID companyId, UUID supplierId, ExpenseType expenseType,
            LocalDate expenseDate, String concept, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
            PaymentCondition paymentCondition, LocalDate dueDate, String evidenceUrl, String idempotencyKey,
            Instant createdAt) {
        return new Expense(id, companyId, supplierId, expenseType, expenseDate, concept, subtotal, taxTotal, total,
                paymentCondition, dueDate, evidenceUrl, ExpenseStatus.PENDING, idempotencyKey, createdAt, null);
    }

    public Expense confirm(Instant confirmedAt) {
        if (status == ExpenseStatus.CONFIRMED) {
            return this;
        }
        return new Expense(id, companyId, supplierId, expenseType, expenseDate, concept, subtotal, taxTotal, total,
                paymentCondition, dueDate, evidenceUrl, ExpenseStatus.CONFIRMED, idempotencyKey, createdAt,
                confirmedAt);
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
