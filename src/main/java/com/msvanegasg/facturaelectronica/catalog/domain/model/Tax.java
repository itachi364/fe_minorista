package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Tax {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TYPE_LENGTH = 50;

    private final Long id;
    private final String name;
    private final BigDecimal percentage;
    private final String type;
    private final Country country;
    private final String description;
    private final boolean active;

    private Tax(Long id, String name, BigDecimal percentage, String type, Country country, String description,
            boolean active) {
        this.id = id;
        this.name = name;
        this.percentage = percentage;
        this.type = type;
        this.country = country;
        this.description = description;
        this.active = active;
    }

    public static Tax create(String name, BigDecimal percentage, String type, Country country, String description) {
        return new Tax(null, normalizeName(name), normalizePercentage(percentage), normalizeType(type),
                requireCountry(country), normalizeDescription(description), true);
    }

    public static Tax restore(Long id, String name, BigDecimal percentage, String type, Country country,
            String description, boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Tax(id, normalizeName(name), normalizePercentage(percentage), normalizeType(type),
                requireCountry(country), normalizeDescription(description), active);
    }

    public Tax update(String name, BigDecimal percentage, String type, Country country, String description) {
        return new Tax(id, normalizeName(name), normalizePercentage(percentage), normalizeType(type),
                requireCountry(country), normalizeDescription(description), active);
    }

    public Tax enable() {
        return new Tax(id, name, percentage, type, country, description, true);
    }

    public Tax disable() {
        return new Tax(id, name, percentage, type, country, description, false);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public BigDecimal percentage() {
        return percentage;
    }

    public String type() {
        return type;
    }

    public Country country() {
        return country;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
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

    private static BigDecimal normalizePercentage(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("percentage must be greater than zero");
        }
        return value;
    }

    private static String normalizeType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_TYPE_LENGTH) {
            throw new IllegalArgumentException("type must be 50 characters or less");
        }
        return normalized;
    }

    private static Country requireCountry(Country value) {
        return Objects.requireNonNull(value, "country is required");
    }

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        return value.trim();
    }
}
