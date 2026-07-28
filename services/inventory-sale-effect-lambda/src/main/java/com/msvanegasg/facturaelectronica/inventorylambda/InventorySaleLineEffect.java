package com.msvanegasg.facturaelectronica.inventorylambda;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record InventorySaleLineEffect(UUID lineId, UUID productId, BigDecimal quantity, BigDecimal unitCost,
        boolean stockTracked) {

    public InventorySaleLineEffect {
        Objects.requireNonNull(lineId, "lineId is required");
        Objects.requireNonNull(productId, "productId is required");
        quantity = requiredPositive(quantity, "quantity");
        unitCost = unitCost == null ? BigDecimal.ZERO : unitCost;
        if (unitCost.signum() < 0) {
            throw new IllegalArgumentException("unitCost must be zero or positive");
        }
    }

    private static BigDecimal requiredPositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
        return value;
    }
}
