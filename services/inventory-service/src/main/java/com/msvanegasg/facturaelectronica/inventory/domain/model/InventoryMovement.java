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
        BigDecimal unitCost,
        BigDecimal previousStock,
        BigDecimal resultingStock,
        InventorySourceDocumentType sourceDocumentType,
        UUID sourceDocumentId,
        String idempotencyKey,
        String reason,
        UUID createdBy,
        Instant movementAt) {

    public InventoryMovement {
        require(id, "id");
        require(companyId, "companyId");
        require(productId, "productId");
        require(movementType, "movementType");
        requirePositive(quantity, "quantity");
        requireNonNegative(unitCost, "unitCost");
        requireNonNegative(previousStock, "previousStock");
        requireNonNegative(resultingStock, "resultingStock");
        require(sourceDocumentType, "sourceDocumentType");
        require(sourceDocumentId, "sourceDocumentId");
        idempotencyKey = normalizeKey(idempotencyKey);
        reason = normalizeReason(reason, movementType);
        require(movementAt, "movementAt");
    }

    public static InventoryMovement from(UUID id, StockBalance previous, StockBalance resulting,
            InventoryMovementType type, BigDecimal quantity, BigDecimal unitCost,
            InventorySourceDocumentType sourceType, UUID sourceId, String idempotencyKey, String reason,
            UUID createdBy, Instant movementAt) {
        return new InventoryMovement(id, previous.companyId(), previous.productId(), type, quantity, unitCost,
                previous.currentStock(), resulting.currentStock(), sourceType, sourceId, idempotencyKey, reason,
                createdBy, movementAt);
    }

    private static String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        return value.trim();
    }

    private static String normalizeReason(String value, InventoryMovementType movementType) {
        if (value == null || value.isBlank()) {
            if (movementType.requiresReason()) {
                throw new IllegalArgumentException("reason is required for " + movementType);
            }
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 300) {
            throw new IllegalArgumentException("reason length is invalid");
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
