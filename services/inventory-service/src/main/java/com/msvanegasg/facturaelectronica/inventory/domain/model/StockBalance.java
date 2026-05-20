package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StockBalance(
        UUID companyId,
        UUID productId,
        BigDecimal currentStock,
        BigDecimal reservedStock,
        BigDecimal averageCost,
        Instant updatedAt) {

    public StockBalance {
        require(companyId, "companyId");
        require(productId, "productId");
        requireNonNegative(currentStock, "currentStock");
        requireNonNegative(reservedStock, "reservedStock");
        requireNonNegative(averageCost, "averageCost");
        require(updatedAt, "updatedAt");
    }

    public static StockBalance empty(UUID companyId, UUID productId, Instant now) {
        return new StockBalance(companyId, productId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, now);
    }

    public StockBalance apply(InventoryMovementType type, BigDecimal quantity, BigDecimal unitCost, Instant now) {
        require(type, "movementType");
        requirePositive(quantity, "quantity");
        requireNonNegative(unitCost, "unitCost");
        BigDecimal resulting = type.increasesStock() ? currentStock.add(quantity) : currentStock.subtract(quantity);
        if (resulting.signum() < 0) {
            throw new IllegalStateException("stock is insufficient for movement");
        }
        BigDecimal newCost = type.increasesStock() && unitCost.signum() > 0 ? unitCost : averageCost;
        return new StockBalance(companyId, productId, resulting, reservedStock, newCost, now);
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
