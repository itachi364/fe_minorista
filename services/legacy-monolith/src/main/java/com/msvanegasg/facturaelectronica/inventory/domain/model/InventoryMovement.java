package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InventoryMovement(
        UUID id,
        UUID companyId,
        UUID productId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        BigDecimal previousStock,
        BigDecimal resultingStock,
        InventorySourceDocumentType sourceDocumentType,
        UUID sourceDocumentId,
        UUID createdBy,
        Instant movementAt) {

    public InventoryMovement {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(productId, "productId");
        requireNonNull(movementType, "movementType");
        requirePositive(quantity, "quantity");
        requireNonNegative(previousStock, "previousStock");
        requireNonNegative(resultingStock, "resultingStock");
        requireNonNull(sourceDocumentType, "sourceDocumentType");
        requireNonNull(sourceDocumentId, "sourceDocumentId");
        requireNonNull(movementAt, "movementAt");
    }

    public static InventoryMovement fromStockChange(
            UUID id,
            StockBalance previousBalance,
            StockBalance resultingBalance,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventorySourceDocumentType sourceDocumentType,
            UUID sourceDocumentId,
            UUID createdBy,
            Instant movementAt) {
        requireNonNull(previousBalance, "previousBalance");
        requireNonNull(resultingBalance, "resultingBalance");

        return new InventoryMovement(
                id,
                previousBalance.companyId(),
                previousBalance.productId(),
                movementType,
                quantity,
                previousBalance.currentStock(),
                resultingBalance.currentStock(),
                sourceDocumentType,
                sourceDocumentId,
                createdBy,
                movementAt);
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
