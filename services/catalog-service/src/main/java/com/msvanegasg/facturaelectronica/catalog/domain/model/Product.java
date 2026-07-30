package com.msvanegasg.facturaelectronica.catalog.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Product {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 250;

    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal basePrice;
    private final int stockQuantity;
    private final Category category;
    private final Long barcode;
    private final boolean active;

    private Product(Long id, String name, String description, BigDecimal basePrice, int stockQuantity,
            Category category, Long barcode, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.barcode = barcode;
        this.active = active;
    }

    public static Product create(String name, String description, BigDecimal basePrice, Integer stockQuantity,
            Category category, Long barcode) {
        return new Product(null, normalizeName(name), normalizeDescription(description), normalizeBasePrice(basePrice),
                normalizeStockQuantity(stockQuantity), requireCategory(category), requireBarcode(barcode), true);
    }

    public static Product restore(Long id, String name, String description, BigDecimal basePrice, Integer stockQuantity,
            Category category, Long barcode, boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Product(id, normalizeName(name), normalizeDescription(description), normalizeBasePrice(basePrice),
                normalizeStockQuantity(stockQuantity), requireCategory(category), requireBarcode(barcode), active);
    }

    public Product update(String name, String description, BigDecimal basePrice, Integer stockQuantity,
            Category category, Long barcode) {
        return new Product(id, normalizeName(name), normalizeDescription(description), normalizeBasePrice(basePrice),
                normalizeStockQuantity(stockQuantity), requireCategory(category), requireBarcode(barcode), active);
    }

    public Product increaseStock(Integer quantityToAdd) {
        int increment = normalizePositiveQuantity(quantityToAdd);
        return new Product(id, name, description, basePrice, stockQuantity + increment, category, barcode, active);
    }

    public Product enable() {
        return new Product(id, name, description, basePrice, stockQuantity, category, barcode, true);
    }

    public Product disable() {
        return new Product(id, name, description, basePrice, stockQuantity, category, barcode, false);
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

    public BigDecimal basePrice() {
        return basePrice;
    }

    public int stockQuantity() {
        return stockQuantity;
    }

    public Category category() {
        return category;
    }

    public Long barcode() {
        return barcode;
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
            throw new IllegalArgumentException("description must be 250 characters or less");
        }
        return normalized;
    }

    private static BigDecimal normalizeBasePrice(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("basePrice must be zero or greater");
        }
        return value;
    }

    private static int normalizeStockQuantity(Integer value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("stockQuantity must be zero or greater");
        }
        return value;
    }

    private static int normalizePositiveQuantity(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("quantityToAdd must be greater than zero");
        }
        return value;
    }

    private static Category requireCategory(Category value) {
        return Objects.requireNonNull(value, "category is required");
    }

    private static Long requireBarcode(Long value) {
        return Objects.requireNonNull(value, "barcode is required");
    }
}
