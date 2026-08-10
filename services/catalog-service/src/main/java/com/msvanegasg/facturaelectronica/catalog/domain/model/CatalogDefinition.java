package com.msvanegasg.facturaelectronica.catalog.domain.model;

public final class CatalogDefinition {

    private static final int MAX_CODE_LENGTH = 80;
    private static final int MAX_LABEL_LENGTH = 180;
    private static final int MAX_DESCRIPTION_LENGTH = 300;

    private final String code;
    private final String label;
    private final String description;
    private final boolean regulatory;
    private final boolean companyConfigurable;
    private final boolean globalEditableByRoot;
    private final boolean active;
    private final int sortOrder;

    private CatalogDefinition(String code, String label, String description, boolean regulatory,
            boolean companyConfigurable, boolean globalEditableByRoot, boolean active, int sortOrder) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.regulatory = regulatory;
        this.companyConfigurable = companyConfigurable;
        this.globalEditableByRoot = globalEditableByRoot;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    public static CatalogDefinition restore(String code, String label, String description, boolean regulatory,
            boolean companyConfigurable, boolean globalEditableByRoot, boolean active, int sortOrder) {
        return new CatalogDefinition(normalizeRequired(code, MAX_CODE_LENGTH, "code"),
                normalizeRequired(label, MAX_LABEL_LENGTH, "label"),
                normalizeOptional(description, MAX_DESCRIPTION_LENGTH, "description"), regulatory,
                companyConfigurable, globalEditableByRoot, active, sortOrder);
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public boolean regulatory() {
        return regulatory;
    }

    public boolean companyConfigurable() {
        return companyConfigurable;
    }

    public boolean globalEditableByRoot() {
        return globalEditableByRoot;
    }

    public boolean active() {
        return active;
    }

    public int sortOrder() {
        return sortOrder;
    }

    private static String normalizeRequired(String value, int maxLength, String fieldName) {
        String normalized = normalizeOptional(value, maxLength, fieldName);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private static String normalizeOptional(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " length is invalid");
        }
        return normalized;
    }
}
