package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.util.Objects;

public final class DocumentType {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 100;

    private final Integer code;
    private final String name;
    private final String description;
    private final boolean active;

    private DocumentType(Integer code, String name, String description, boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public static DocumentType create(Integer code, String name, String description) {
        return new DocumentType(normalizeCode(code), normalizeName(name), normalizeDescription(description), true);
    }

    public static DocumentType restore(Integer code, String name, String description, boolean active) {
        return new DocumentType(normalizeCode(code), normalizeName(name), normalizeDescription(description), active);
    }

    public DocumentType update(String name, String description) {
        return new DocumentType(code, normalizeName(name), normalizeDescription(description), active);
    }

    public DocumentType enable() {
        return new DocumentType(code, name, description, true);
    }

    public DocumentType disable() {
        return new DocumentType(code, name, description, false);
    }

    public Integer code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
    }

    private static Integer normalizeCode(Integer value) {
        requireNonNull(value, "code");
        if (value < 1 || value > 99) {
            throw new IllegalArgumentException("code must be between 1 and 99");
        }
        return value;
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must be 50 characters or less");
        }
        return normalized;
    }

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be 100 characters or less");
        }
        return normalized;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
