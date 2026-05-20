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
        BigDecimal salePrice,
        BigDecimal cost,
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
        requireMoney(salePrice, "salePrice");
        requireMoney(cost, "cost");
        require(createdAt, "createdAt");
        require(updatedAt, "updatedAt");
    }

    public static Product create(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            BigDecimal salePrice, BigDecimal cost, Instant now) {
        return new Product(id, companyId, sku, barcode, name, description, salePrice, cost, true, now, now);
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
