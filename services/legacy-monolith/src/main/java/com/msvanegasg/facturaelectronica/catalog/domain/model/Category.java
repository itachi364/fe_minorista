package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.util.Objects;

public final class Category {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final Long id;
    private final String name;
    private final String description;
    private final boolean active;

    private Category(Long id, String name, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public static Category create(String name, String description) {
        return new Category(null, normalizeName(name), normalizeDescription(description), true);
    }

    public static Category restore(Long id, String name, String description, boolean active) {
        requireNonNull(id, "id");
        return new Category(id, normalizeName(name), normalizeDescription(description), active);
    }

    public Category update(String name, String description) {
        return new Category(id, normalizeName(name), normalizeDescription(description), active);
    }

    public Category enable() {
        return new Category(id, name, description, true);
    }

    public Category disable() {
        return new Category(id, name, description, false);
    }

    public Long id() {
        return id;
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

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be 255 characters or less");
        }
        return normalized;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
