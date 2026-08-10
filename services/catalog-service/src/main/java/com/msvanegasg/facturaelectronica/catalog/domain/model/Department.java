package com.msvanegasg.facturaelectronica.catalog.domain.model;

public final class Department {

    private final String code;
    private final String name;
    private final boolean active;
    private final String source;
    private final String sourceVersion;
    private final int sortOrder;

    private Department(String code, String name, boolean active, String source, String sourceVersion, int sortOrder) {
        this.code = normalize(code, 2, "code");
        this.name = normalize(name, 120, "name");
        this.active = active;
        this.source = normalize(source, 80, "source");
        this.sourceVersion = normalize(sourceVersion, 40, "sourceVersion");
        this.sortOrder = sortOrder;
    }

    public static Department restore(String code, String name, boolean active, String source, String sourceVersion,
            int sortOrder) {
        return new Department(code, name, active, source, sourceVersion, sortOrder);
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public boolean active() {
        return active;
    }

    public String source() {
        return source;
    }

    public String sourceVersion() {
        return sourceVersion;
    }

    public int sortOrder() {
        return sortOrder;
    }

    private static String normalize(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " length is invalid");
        }
        return normalized;
    }
}
