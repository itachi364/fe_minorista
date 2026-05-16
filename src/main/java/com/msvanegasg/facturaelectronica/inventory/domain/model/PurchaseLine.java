package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class PurchaseLine {

    private final Long id;
    private final Long barcode;
    private final Long productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal lineTotal;
    private final boolean active;

    private PurchaseLine(Long id, Long barcode, Long productId, Integer quantity, BigDecimal unitPrice,
            BigDecimal subtotal, BigDecimal tax, BigDecimal lineTotal, boolean active) {
        this.id = id;
        this.barcode = barcode;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.tax = tax;
        this.lineTotal = lineTotal;
        this.active = active;
    }

    public static PurchaseLine create(Long barcode, Long productId, Integer quantity, BigDecimal unitPrice,
            BigDecimal subtotal, BigDecimal tax, BigDecimal lineTotal) {
        return new PurchaseLine(null, requireLong(barcode, "barcode"), requireLong(productId, "productId"),
                requirePositive(quantity, "quantity"), requireNonNegative(unitPrice, "unitPrice"),
                requireNonNegative(subtotal, "subtotal"), requireNonNegative(tax, "tax"),
                requireNonNegative(lineTotal, "lineTotal"), true);
    }

    public static PurchaseLine restore(Long id, Long productId, Integer quantity, BigDecimal unitPrice,
            BigDecimal subtotal, BigDecimal tax, BigDecimal lineTotal, boolean active) {
        return new PurchaseLine(id, productId, requireLong(productId, "productId"), requirePositive(quantity, "quantity"),
                requireNonNegative(unitPrice, "unitPrice"), requireNonNegative(subtotal, "subtotal"),
                requireNonNegative(tax, "tax"), requireNonNegative(lineTotal, "lineTotal"), active);
    }

    public Long id() {
        return id;
    }

    public Long barcode() {
        return barcode;
    }

    public Long productId() {
        return productId;
    }

    public Integer quantity() {
        return quantity;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal tax() {
        return tax;
    }

    public BigDecimal lineTotal() {
        return lineTotal;
    }

    public boolean active() {
        return active;
    }

    private static Long requireLong(Long value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " is required");
    }

    private static Integer requirePositive(Integer value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or positive");
        }
        return value;
    }
}
