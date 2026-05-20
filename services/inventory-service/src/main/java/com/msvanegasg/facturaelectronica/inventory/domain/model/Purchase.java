package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Purchase(
        UUID id,
        UUID companyId,
        UUID supplierId,
        PurchaseStatus status,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        String evidenceUrl,
        String idempotencyKey,
        Instant createdAt,
        Instant confirmedAt,
        List<PurchaseLine> lines) {

    public Purchase {
        require(id, "id");
        require(companyId, "companyId");
        require(status, "status");
        requireMoney(subtotal, "subtotal");
        requireMoney(taxTotal, "taxTotal");
        requireMoney(total, "total");
        idempotencyKey = normalizeKey(idempotencyKey);
        require(createdAt, "createdAt");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines are required"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
    }

    public static Purchase pending(UUID id, UUID companyId, UUID supplierId, BigDecimal subtotal, BigDecimal taxTotal,
            BigDecimal total, String evidenceUrl, String idempotencyKey, Instant createdAt, List<PurchaseLine> lines) {
        return new Purchase(id, companyId, supplierId, PurchaseStatus.PENDING, subtotal, taxTotal, total, evidenceUrl,
                idempotencyKey, createdAt, null, lines.stream().map(line -> line.attachTo(id)).toList());
    }

    public Purchase confirm(Instant confirmedAt) {
        if (status == PurchaseStatus.CONFIRMED) {
            return this;
        }
        return new Purchase(id, companyId, supplierId, PurchaseStatus.CONFIRMED, subtotal, taxTotal, total, evidenceUrl,
                idempotencyKey, createdAt, confirmedAt, lines);
    }

    private static String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        return value.trim();
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
