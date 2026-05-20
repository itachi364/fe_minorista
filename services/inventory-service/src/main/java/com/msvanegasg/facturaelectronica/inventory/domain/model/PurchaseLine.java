package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record PurchaseLine(
        UUID id,
        UUID purchaseId,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total) {

    public PurchaseLine {
        require(id, "id");
        require(productId, "productId");
        requirePositive(quantity, "quantity");
        requireMoney(unitCost, "unitCost");
        requireMoney(subtotal, "subtotal");
        requireMoney(tax, "tax");
        requireMoney(total, "total");
    }

    public PurchaseLine attachTo(UUID purchaseId) {
        return new PurchaseLine(id, purchaseId, productId, quantity, unitCost, subtotal, tax, total);
    }

    private static void requirePositive(BigDecimal value, String field) {
        require(value, field);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
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
