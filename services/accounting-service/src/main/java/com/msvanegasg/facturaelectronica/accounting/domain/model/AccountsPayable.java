package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AccountsPayable(
        UUID id,
        UUID companyId,
        UUID supplierId,
        AccountingSourceType sourceType,
        UUID sourceId,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        AccountsPayableStatus status,
        Instant createdAt) {

    public AccountsPayable {
        require(id, "id");
        require(companyId, "companyId");
        require(sourceType, "sourceType");
        require(sourceId, "sourceId");
        require(issueDate, "issueDate");
        require(dueDate, "dueDate");
        requireMoney(totalAmount, "totalAmount");
        requireMoney(paidAmount, "paidAmount");
        require(status, "status");
        require(createdAt, "createdAt");
        if (totalAmount.signum() <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than zero");
        }
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("paidAmount cannot exceed totalAmount");
        }
    }

    public static AccountsPayable open(UUID id, UUID companyId, UUID supplierId, AccountingSourceType sourceType,
            UUID sourceId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalAmount, Instant createdAt) {
        return new AccountsPayable(id, companyId, supplierId, sourceType, sourceId, issueDate, dueDate, totalAmount,
                BigDecimal.ZERO, AccountsPayableStatus.OPEN, createdAt);
    }

    public AccountsPayable applyPayment(BigDecimal amount) {
        requireMoney(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("payment amount must be greater than zero");
        }
        BigDecimal newPaidAmount = paidAmount.add(amount);
        if (newPaidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalStateException("payment amount exceeds payable balance");
        }
        AccountsPayableStatus newStatus = newPaidAmount.compareTo(totalAmount) == 0 ? AccountsPayableStatus.PAID
                : AccountsPayableStatus.PARTIALLY_PAID;
        return new AccountsPayable(id, companyId, supplierId, sourceType, sourceId, issueDate, dueDate, totalAmount,
                newPaidAmount, newStatus, createdAt);
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

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
