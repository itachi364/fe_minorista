package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public final class AccountingEntryLine {

    private final UUID id;
    private final UUID accountId;
    private final String accountCode;
    private final String accountName;
    private final UUID thirdpartyId;
    private final BigDecimal debitAmount;
    private final BigDecimal creditAmount;
    private final String description;

    private AccountingEntryLine(
            UUID id,
            UUID accountId,
            String accountCode,
            String accountName,
            UUID thirdpartyId,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            String description) {
        this.id = id;
        this.accountId = accountId;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.thirdpartyId = thirdpartyId;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.description = description;
    }

    public static AccountingEntryLine create(
            UUID id,
            UUID accountId,
            String accountCode,
            String accountName,
            UUID thirdpartyId,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            String description) {
        requireNonNull(id, "id");
        requireNonNull(accountId, "accountId");
        String normalizedAccountCode = normalizeRequired(accountCode, "accountCode");
        String normalizedAccountName = normalizeRequired(accountName, "accountName");
        BigDecimal normalizedDebit = normalizeAmount(debitAmount, "debitAmount");
        BigDecimal normalizedCredit = normalizeAmount(creditAmount, "creditAmount");
        if (normalizedDebit.signum() > 0 && normalizedCredit.signum() > 0) {
            throw new IllegalArgumentException("accounting line cannot have debit and credit amounts at the same time");
        }
        if (normalizedDebit.signum() == 0 && normalizedCredit.signum() == 0) {
            throw new IllegalArgumentException("accounting line requires debit or credit amount");
        }
        return new AccountingEntryLine(
                id,
                accountId,
                normalizedAccountCode,
                normalizedAccountName,
                thirdpartyId,
                normalizedDebit,
                normalizedCredit,
                normalizeOptional(description));
    }

    public UUID id() {
        return id;
    }

    public UUID accountId() {
        return accountId;
    }

    public String accountCode() {
        return accountCode;
    }

    public String accountName() {
        return accountName;
    }

    public UUID thirdpartyId() {
        return thirdpartyId;
    }

    public BigDecimal debitAmount() {
        return debitAmount;
    }

    public BigDecimal creditAmount() {
        return creditAmount;
    }

    public String description() {
        return description;
    }

    private static BigDecimal normalizeAmount(BigDecimal amount, String fieldName) {
        requireNonNull(amount, fieldName);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
