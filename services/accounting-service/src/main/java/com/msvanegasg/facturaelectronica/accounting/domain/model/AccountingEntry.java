package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AccountingEntry {

    private final UUID id;
    private final UUID companyId;
    private final LocalDate entryDate;
    private final String description;
    private final AccountingSourceType sourceType;
    private final UUID sourceId;
    private final UUID accountingRuleId;
    private final AccountingEntryStatus status;
    private final List<AccountingEntryLine> lines;

    private AccountingEntry(
            UUID id,
            UUID companyId,
            LocalDate entryDate,
            String description,
            AccountingSourceType sourceType,
            UUID sourceId,
            UUID accountingRuleId,
            AccountingEntryStatus status,
            List<AccountingEntryLine> lines) {
        this.id = id;
        this.companyId = companyId;
        this.entryDate = entryDate;
        this.description = description;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.accountingRuleId = accountingRuleId;
        this.status = status;
        this.lines = List.copyOf(lines);
    }

    public static AccountingEntry post(
            UUID id,
            UUID companyId,
            LocalDate entryDate,
            String description,
            AccountingSourceType sourceType,
            UUID sourceId,
            List<AccountingEntryLine> lines) {
        return post(id, companyId, entryDate, description, sourceType, sourceId, null, lines);
    }

    public static AccountingEntry post(
            UUID id,
            UUID companyId,
            LocalDate entryDate,
            String description,
            AccountingSourceType sourceType,
            UUID sourceId,
            UUID accountingRuleId,
            List<AccountingEntryLine> lines) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(entryDate, "entryDate");
        requireNonNull(sourceType, "sourceType");
        requireNonNull(sourceId, "sourceId");
        String normalizedDescription = normalizeDescription(description);
        List<AccountingEntryLine> normalizedLines = validateLines(lines);
        BigDecimal debitTotal = totalDebit(normalizedLines);
        BigDecimal creditTotal = totalCredit(normalizedLines);
        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new IllegalStateException("accounting entry must be balanced");
        }

        return new AccountingEntry(
                id,
                companyId,
                entryDate,
                normalizedDescription,
                sourceType,
                sourceId,
                accountingRuleId,
                AccountingEntryStatus.POSTED,
                normalizedLines);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public LocalDate entryDate() {
        return entryDate;
    }

    public String description() {
        return description;
    }

    public AccountingSourceType sourceType() {
        return sourceType;
    }

    public UUID sourceId() {
        return sourceId;
    }

    public UUID accountingRuleId() {
        return accountingRuleId;
    }

    public AccountingEntryStatus status() {
        return status;
    }

    public List<AccountingEntryLine> lines() {
        return lines;
    }

    public BigDecimal debitTotal() {
        return totalDebit(lines);
    }

    public BigDecimal creditTotal() {
        return totalCredit(lines);
    }

    private static List<AccountingEntryLine> validateLines(List<AccountingEntryLine> lines) {
        if (lines == null || lines.size() < 2) {
            throw new IllegalArgumentException("accounting entry requires at least two lines");
        }
        return List.copyOf(lines);
    }

    private static BigDecimal totalDebit(List<AccountingEntryLine> lines) {
        return lines.stream()
                .map(AccountingEntryLine::debitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalCredit(List<AccountingEntryLine> lines) {
        return lines.stream()
                .map(AccountingEntryLine::creditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("accounting entry description is required");
        }
        return description.trim();
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
