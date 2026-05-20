package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.util.Objects;

public final class Country {

    private static final int MAX_CODE_LENGTH = 10;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_CURRENCY_LENGTH = 50;

    private final String code;
    private final String name;
    private final String currency;
    private final boolean active;

    private Country(String code, String name, String currency, boolean active) {
        this.code = code;
        this.name = name;
        this.currency = currency;
        this.active = active;
    }

    public static Country create(String code, String name, String currency) {
        return new Country(normalizeCode(code), normalizeName(name), normalizeCurrency(currency), true);
    }

    public static Country restore(String code, String name, String currency, boolean active) {
        return new Country(normalizeCode(code), normalizeName(name), normalizeCurrency(currency), active);
    }

    public Country update(String name, String currency) {
        return new Country(code, normalizeName(name), normalizeCurrency(currency), active);
    }

    public Country enable() {
        return new Country(code, name, currency, true);
    }

    public Country disable() {
        return new Country(code, name, currency, false);
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String currency() {
        return currency;
    }

    public boolean active() {
        return active;
    }

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("code must be 10 characters or less");
        }
        return normalized;
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must be 100 characters or less");
        }
        return normalized;
    }

    private static String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_CURRENCY_LENGTH) {
            throw new IllegalArgumentException("currency must be 50 characters or less");
        }
        return normalized;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
