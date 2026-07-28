package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AccountsReceivable(
        UUID id,
        UUID companyId,
        UUID customerId,
        AccountingSourceType sourceType,
        UUID sourceId,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        AccountsReceivableStatus status,
        String idempotencyKey,
        Instant createdAt) {

    public AccountsReceivable {
        require(id, "id");
        require(companyId, "companyId");
        require(customerId, "customerId");
        require(sourceType, "sourceType");
        require(sourceId, "sourceId");
        require(issueDate, "issueDate");
        require(dueDate, "dueDate");
        requireMoney(totalAmount, "totalAmount");
        requireMoney(paidAmount, "paidAmount");
        require(status, "status");
        idempotencyKey = normalizeRequired(idempotencyKey, 120, "idempotencyKey");
        require(createdAt, "createdAt");
        if (totalAmount.signum() <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than zero");
        }
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("paidAmount cannot exceed totalAmount");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("dueDate cannot be before issueDate");
        }
    }

    public static AccountsReceivable open(UUID id, UUID companyId, UUID customerId, AccountingSourceType sourceType,
            UUID sourceId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalAmount, String idempotencyKey,
            Instant createdAt) {
        return new AccountsReceivable(id, companyId, customerId, sourceType, sourceId, issueDate, dueDate, totalAmount,
                BigDecimal.ZERO, AccountsReceivableStatus.OPEN, idempotencyKey, createdAt);
    }

    public AccountsReceivable applyPayment(BigDecimal amount) {
        requireMoney(amount, "amount");
        if (status == AccountsReceivableStatus.CANCELLED) {
            throw new IllegalStateException("accounts receivable is cancelled");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("payment amount must be greater than zero");
        }
        BigDecimal newPaidAmount = paidAmount.add(amount);
        if (newPaidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalStateException("payment amount exceeds receivable balance");
        }
        AccountsReceivableStatus newStatus = newPaidAmount.compareTo(totalAmount) == 0
                ? AccountsReceivableStatus.PAID
                : AccountsReceivableStatus.PARTIALLY_PAID;
        return new AccountsReceivable(id, companyId, customerId, sourceType, sourceId, issueDate, dueDate, totalAmount,
                newPaidAmount, newStatus, idempotencyKey, createdAt);
    }

    public BigDecimal balance() {
        return totalAmount.subtract(paidAmount);
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