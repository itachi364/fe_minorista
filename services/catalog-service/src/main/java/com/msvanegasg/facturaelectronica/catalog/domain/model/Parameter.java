package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.util.Objects;

public final class Parameter {

    private static final int MAX_KEY_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final Long id;
    private final String key;
    private final String value;
    private final String description;
    private final boolean active;

    private Parameter(Long id, String key, String value, String description, boolean active) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
        this.active = active;
    }

    public static Parameter create(String key, String value, String description) {
        return new Parameter(null, normalizeKey(key), normalizeValue(value), normalizeDescription(description), true);
    }

    public static Parameter restore(Long id, String key, String value, String description, boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Parameter(id, normalizeKey(key), normalizeValue(value), normalizeDescription(description), active);
    }

    public Parameter update(String key, String value, String description) {
        return new Parameter(id, normalizeKey(key), normalizeValue(value), normalizeDescription(description), active);
    }

    public Parameter enable() {
        return new Parameter(id, key, value, description, true);
    }

    public Parameter disable() {
        return new Parameter(id, key, value, description, false);
    }

    public Long id() {
        return id;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
    }

    private static String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("key is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("key must be 100 characters or less");
        }
        return normalized;
    }

    private static String normalizeValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value is required");
        }
        return value.trim();
    }

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be 500 characters or less");
        }
        return normalized;
    }
}
