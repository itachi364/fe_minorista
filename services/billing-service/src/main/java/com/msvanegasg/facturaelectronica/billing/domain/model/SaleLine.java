package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public record SaleLine(UUID id, UUID productId, String productSku, String productName, SaleItemType itemType,
        boolean stockTracked, BigDecimal quantity, BigDecimal unitPrice, BigDecimal unitCost,
        BigDecimal discountAmount, String taxCode, BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount,
        BigDecimal total) {

    public SaleLine {
        require(id, "id");
        require(productId, "productId");
        productSku = normalizeOptional(productSku, 80, "productSku");
        productName = normalizeOptional(productName, 160, "productName");
        itemType = itemType == null ? SaleItemType.PHYSICAL_GOOD : itemType;
        requirePositive(quantity, "quantity");
        requireNonNegative(unitPrice, "unitPrice");
        unitCost = unitCost == null ? BigDecimal.ZERO : unitCost;
        requireNonNegative(unitCost, "unitCost");
        requireNonNegative(discountAmount, "discountAmount");
        require(taxCode, "taxCode");
        requireNonNegative(taxRate, "taxRate");
        requireNonNegative(subtotal, "subtotal");
        requireNonNegative(taxAmount, "taxAmount");
        requireNonNegative(total, "total");
    }

    public static SaleLine calculate(UUID id, UUID productId, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal discountAmount, String taxCode, BigDecimal taxRate) {
        return calculate(id, productId, null, null, SaleItemType.PHYSICAL_GOOD, true, quantity, unitPrice,
                BigDecimal.ZERO, discountAmount, taxCode, taxRate);
    }

    public static SaleLine calculate(UUID id, UUID productId, String productSku, String productName,
            SaleItemType itemType, boolean stockTracked, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal discountAmount, String taxCode, BigDecimal taxRate) {
        return calculate(id, productId, productSku, productName, itemType, stockTracked, quantity, unitPrice,
                BigDecimal.ZERO, discountAmount, taxCode, taxRate);
    }

    public static SaleLine calculate(UUID id, UUID productId, String productSku, String productName,
            SaleItemType itemType, boolean stockTracked, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal unitCost, BigDecimal discountAmount, String taxCode, BigDecimal taxRate) {
        requirePositive(quantity, "quantity");
        requireNonNegative(unitPrice, "unitPrice");
        BigDecimal safeUnitCost = unitCost == null ? BigDecimal.ZERO : unitCost;
        requireNonNegative(safeUnitCost, "unitCost");
        BigDecimal safeDiscount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal safeTaxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        BigDecimal gross = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        if (safeDiscount.compareTo(gross) > 0) {
            throw new IllegalArgumentException("discountAmount cannot exceed line gross amount");
        }
        BigDecimal taxable = gross.subtract(safeDiscount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = taxable.multiply(safeTaxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new SaleLine(id, productId, productSku, productName, itemType, stockTracked, quantity, unitPrice,
                safeUnitCost, safeDiscount,
                taxCode == null || taxCode.isBlank() ? "NO_TAX" : taxCode, safeTaxRate, taxable, tax,
                taxable.add(tax).setScale(2, RoundingMode.HALF_UP));
    }

    public boolean affectsInventory() {
        return stockTracked;
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

    private static void requirePositive(BigDecimal value, String field) {
        require(value, field);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        require(value, field);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be zero or positive");
        }
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
