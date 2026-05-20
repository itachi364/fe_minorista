package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class StockBalance {

    private final UUID companyId;
    private final UUID productId;
    private final BigDecimal currentStock;
    private final BigDecimal reservedStock;

    private StockBalance(UUID companyId, UUID productId, BigDecimal currentStock, BigDecimal reservedStock) {
        this.companyId = companyId;
        this.productId = productId;
        this.currentStock = currentStock;
        this.reservedStock = reservedStock;
    }

    public static StockBalance restore(
            UUID companyId,
            UUID productId,
            BigDecimal currentStock,
            BigDecimal reservedStock) {
        requireNonNull(companyId, "companyId");
        requireNonNull(productId, "productId");
        requireNonNegative(currentStock, "currentStock");
        requireNonNegative(reservedStock, "reservedStock");
        return new StockBalance(companyId, productId, currentStock, reservedStock);
    }

    public static StockBalance empty(UUID companyId, UUID productId) {
        return restore(companyId, productId, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public StockBalance apply(InventoryMovementType movementType, BigDecimal quantity) {
        requireNonNull(movementType, "movementType");
        requirePositive(quantity, "quantity");

        BigDecimal resultingStock = switch (movementType) {
            case PURCHASE_IN, RETURN_IN, ADJUSTMENT_IN -> currentStock.add(quantity);
            case SALE_OUT, ADJUSTMENT_OUT -> currentStock.subtract(quantity);
        };

        if (resultingStock.signum() < 0) {
            throw new IllegalStateException("stock is insufficient for movement");
        }

        return new StockBalance(companyId, productId, resultingStock, reservedStock);
    }

    public UUID companyId() {
        return companyId;
    }

    public UUID productId() {
        return productId;
    }

    public BigDecimal currentStock() {
        return currentStock;
    }

    public BigDecimal reservedStock() {
        return reservedStock;
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
