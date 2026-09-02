package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Product(
        UUID id,
        UUID companyId,
        String sku,
        String barcode,
        String name,
        String description,
        InventoryItemType itemType,
        boolean saleEnabled,
        boolean purchaseEnabled,
        boolean stockTracked,
        BigDecimal salePrice,
        BigDecimal cost,
        String taxCategoryCode,
        String taxCode,
        String taxLabel,
        BigDecimal taxRate,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public Product {
        require(id, "id");
        require(companyId, "companyId");
        sku = normalizeRequired(sku, 80, "sku");
        barcode = normalizeOptional(barcode, 80, "barcode");
        name = normalizeRequired(name, 160, "name");
        description = normalizeOptional(description, 500, "description");
        itemType = itemType == null ? InventoryItemType.PHYSICAL_GOOD : itemType;
        if (itemType == InventoryItemType.SERVICE && stockTracked) {
            throw new IllegalArgumentException("services must not track stock automatically");
        }
        requireMoney(salePrice, "salePrice");
        requireMoney(cost, "cost");
        taxCategoryCode = normalizeRequired(taxCategoryCode, 40, "taxCategoryCode");
        taxCode = normalizeRequired(taxCode, 80, "taxCode");
        taxLabel = normalizeRequired(taxLabel, 180, "taxLabel");
        requireMoney(taxRate, "taxRate");
        require(createdAt, "createdAt");
        require(updatedAt, "updatedAt");
    }

    public static Product create(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            InventoryItemType itemType, boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked,
            BigDecimal salePrice, BigDecimal cost, String taxCategoryCode, String taxCode, String taxLabel,
            BigDecimal taxRate, Instant now) {
        return new Product(id, companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled,
                stockTracked, salePrice, cost, taxCategoryCode, taxCode, taxLabel, taxRate, true, now, now);
    }

    public static Product create(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            InventoryItemType itemType, boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked,
            BigDecimal salePrice, BigDecimal cost, Instant now) {
        return create(id, companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled,
                stockTracked, salePrice, cost, "IVA", "IVA_19", "IVA 19%", new BigDecimal("19"), now);
    }

    public Product update(String sku, String barcode, String name, String description, InventoryItemType itemType,
            boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked, BigDecimal salePrice, BigDecimal cost,
            String taxCategoryCode, String taxCode, String taxLabel, BigDecimal taxRate, Instant updatedAt) {
        return new Product(id, companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled,
                stockTracked, salePrice, cost, taxCategoryCode, taxCode, taxLabel, taxRate, active, createdAt,
                updatedAt);
    }

    public Product deactivate(Instant updatedAt) {
        return new Product(id, companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled,
                stockTracked, salePrice, cost, taxCategoryCode, taxCode, taxLabel, taxRate, false, createdAt,
                updatedAt);
    }

    private static String normalizeRequired(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalizeOptional(value, maxLength, field);
    }

    private static String normalizeOptional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " length is invalid");
        }
        return normalized;
    }

    private static void requireMoney(BigDecimal value, String field) {
        require(value, field);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be zero or positive");
        }
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
