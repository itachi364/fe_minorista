package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.Objects;

public final class AccountingRuleLine {

    private final String accountCode;
    private final AccountingEntrySide side;
    private final AccountingAmountType amountType;
    private final String description;

    private AccountingRuleLine(
            String accountCode,
            AccountingEntrySide side,
            AccountingAmountType amountType,
            String description) {
        this.accountCode = accountCode;
        this.side = side;
        this.amountType = amountType;
        this.description = description;
    }

    public static AccountingRuleLine create(
            String accountCode,
            AccountingEntrySide side,
            AccountingAmountType amountType,
            String description) {
        String normalizedAccountCode = normalizeAccountCode(accountCode);
        Objects.requireNonNull(side, "side is required");
        Objects.requireNonNull(amountType, "amountType is required");
        return new AccountingRuleLine(normalizedAccountCode, side, amountType, normalizeOptional(description));
    }

    public String accountCode() {
        return accountCode;
    }

    public AccountingEntrySide side() {
        return side;
    }

    public AccountingAmountType amountType() {
        return amountType;
    }

    public String description() {
        return description;
    }

    private static String normalizeAccountCode(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            throw new IllegalArgumentException("account code is required");
        }
        String normalized = accountCode.trim();
        if (!normalized.matches("\\d+")) {
            throw new IllegalArgumentException("account code must contain only digits");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }
}
