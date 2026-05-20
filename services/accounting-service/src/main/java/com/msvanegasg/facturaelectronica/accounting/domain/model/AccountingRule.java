package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AccountingRule {

    private final UUID id;
    private final UUID companyId;
    private final AccountingEventType eventType;
    private final AccountingSourceType sourceType;
    private final String name;
    private final List<AccountingRuleLine> lines;
    private final boolean active;

    private AccountingRule(
            UUID id,
            UUID companyId,
            AccountingEventType eventType,
            AccountingSourceType sourceType,
            String name,
            List<AccountingRuleLine> lines,
            boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.name = name;
        this.lines = List.copyOf(lines);
        this.active = active;
    }

    public static AccountingRule create(
            UUID id,
            UUID companyId,
            AccountingEventType eventType,
            AccountingSourceType sourceType,
            String name,
            List<AccountingRuleLine> lines) {
        return restore(id, companyId, eventType, sourceType, name, lines, true);
    }

    public static AccountingRule restore(
            UUID id,
            UUID companyId,
            AccountingEventType eventType,
            AccountingSourceType sourceType,
            String name,
            List<AccountingRuleLine> lines,
            boolean active) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(eventType, "eventType");
        requireNonNull(sourceType, "sourceType");
        String normalizedName = normalizeName(name);
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("accounting rule requires lines");
        }
        return new AccountingRule(id, companyId, eventType, sourceType, normalizedName, lines, active);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public AccountingEventType eventType() {
        return eventType;
    }

    public AccountingSourceType sourceType() {
        return sourceType;
    }

    public String name() {
        return name;
    }

    public List<AccountingRuleLine> lines() {
        return lines;
    }

    public boolean active() {
        return active;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("accounting rule name is required");
        }
        return name.trim();
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
