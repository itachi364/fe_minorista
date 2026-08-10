package com.msvanegasg.facturaelectronica.catalog.domain.model;

public final class Municipality {

    private final String code;
    private final String departmentCode;
    private final String name;
    private final boolean active;
    private final String source;
    private final String sourceVersion;
    private final int sortOrder;

    private Municipality(String code, String departmentCode, String name, boolean active, String source,
            String sourceVersion, int sortOrder) {
        this.code = normalize(code, 5, "code");
        this.departmentCode = normalize(departmentCode, 2, "departmentCode");
        this.name = normalize(name, 160, "name");
        this.active = active;
        this.source = normalize(source, 80, "source");
        this.sourceVersion = normalize(sourceVersion, 40, "sourceVersion");
        this.sortOrder = sortOrder;
    }

    public static Municipality restore(String code, String departmentCode, String name, boolean active, String source,
            String sourceVersion, int sortOrder) {
        return new Municipality(code, departmentCode, name, active, source, sourceVersion, sortOrder);
    }

    public String code() {
        return code;
    }

    public String departmentCode() {
        return departmentCode;
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
