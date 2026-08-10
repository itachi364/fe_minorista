package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.time.LocalDate;

public final class CatalogItem {

    private static final int MAX_CODE_LENGTH = 80;
    private static final int MAX_LABEL_LENGTH = 180;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MAX_SOURCE_LENGTH = 80;
    private static final int MAX_SOURCE_VERSION_LENGTH = 40;

    private final String catalogCode;
    private final String itemCode;
    private final String label;
    private final String description;
    private final boolean active;
    private final boolean regulatory;
    private final String source;
    private final String sourceVersion;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private final int sortOrder;

    private CatalogItem(String catalogCode, String itemCode, String label, String description, boolean active,
            boolean regulatory, String source, String sourceVersion, LocalDate validFrom, LocalDate validTo,
            int sortOrder) {
        this.catalogCode = catalogCode;
        this.itemCode = itemCode;
        this.label = label;
        this.description = description;
        this.active = active;
        this.regulatory = regulatory;
        this.source = source;
        this.sourceVersion = sourceVersion;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.sortOrder = sortOrder;
    }

    public static CatalogItem restore(String catalogCode, String itemCode, String label, String description,
            boolean active, boolean regulatory, String source, String sourceVersion, int sortOrder) {
        return restore(catalogCode, itemCode, label, description, active, regulatory, source, sourceVersion, null,
                null, sortOrder);
    }

    public static CatalogItem restore(String catalogCode, String itemCode, String label, String description,
            boolean active, boolean regulatory, String source, String sourceVersion, LocalDate validFrom,
            LocalDate validTo, int sortOrder) {
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo cannot be before validFrom");
        }
        return new CatalogItem(normalizeRequired(catalogCode, MAX_CODE_LENGTH, "catalogCode"),
                normalizeRequired(itemCode, MAX_CODE_LENGTH, "itemCode"),
                normalizeRequired(label, MAX_LABEL_LENGTH, "label"),
                normalizeOptional(description, MAX_DESCRIPTION_LENGTH, "description"), active, regulatory,
                normalizeRequired(source, MAX_SOURCE_LENGTH, "source"),
                normalizeRequired(sourceVersion, MAX_SOURCE_VERSION_LENGTH, "sourceVersion"), validFrom, validTo,
                sortOrder);
    }

    public String catalogCode() {
        return catalogCode;
    }

    public String itemCode() {
        return itemCode;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
    }

    public boolean regulatory() {
        return regulatory;
    }

    public String source() {
        return source;
    }

    public String sourceVersion() {
        return sourceVersion;
    }

    public LocalDate validFrom() {
        return validFrom;
    }

    public LocalDate validTo() {
        return validTo;
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
