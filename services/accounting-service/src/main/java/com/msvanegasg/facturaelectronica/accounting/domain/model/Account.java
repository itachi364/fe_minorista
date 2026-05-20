package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Account {

    private final UUID id;
    private final UUID companyId;
    private final String code;
    private final String name;
    private final AccountCategory category;
    private final AccountLevel level;
    private final AccountNature nature;
    private final UUID parentAccountId;
    private final boolean active;

    private Account(
            UUID id,
            UUID companyId,
            String code,
            String name,
            AccountCategory category,
            AccountLevel level,
            AccountNature nature,
            UUID parentAccountId,
            boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.category = category;
        this.level = level;
        this.nature = nature;
        this.parentAccountId = parentAccountId;
        this.active = active;
    }

    public static Account create(
            UUID id,
            UUID companyId,
            String code,
            String name,
            UUID parentAccountId) {
        return restore(id, companyId, code, name, parentAccountId, true);
    }

    public static Account restore(
            UUID id,
            UUID companyId,
            String code,
            String name,
            UUID parentAccountId,
            boolean active) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        String normalizedCode = normalizeCode(code);
        String normalizedName = normalizeName(name);
        AccountCategory category = PucAccountClassifier.categoryOf(normalizedCode);
        AccountLevel level = PucAccountClassifier.levelOf(normalizedCode);
        AccountNature nature = PucAccountClassifier.natureOf(category);

        return new Account(
                id,
                companyId,
                normalizedCode,
                normalizedName,
                category,
                level,
                nature,
                parentAccountId,
                active);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public AccountCategory category() {
        return category;
    }

    public AccountLevel level() {
        return level;
    }

    public AccountNature nature() {
        return nature;
    }

    public UUID parentAccountId() {
        return parentAccountId;
    }

    public boolean active() {
        return active;
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("account code is required");
        }
        String normalizedCode = code.trim();
        if (!normalizedCode.matches("\\d+")) {
            throw new IllegalArgumentException("account code must contain only digits");
        }
        return normalizedCode;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("account name is required");
        }
        return name.trim();
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
