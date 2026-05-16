package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class Purchase {

    private final Long id;
    private final Long supplierId;
    private final LocalDateTime date;
    private final BigDecimal subtotal;
    private final BigDecimal taxTotal;
    private final BigDecimal total;
    private final String evidenceUrl;
    private final PurchaseStatus status;
    private final boolean active;
    private final List<PurchaseLine> lines;

    private Purchase(Long id, Long supplierId, LocalDateTime date, BigDecimal subtotal, BigDecimal taxTotal,
            BigDecimal total, String evidenceUrl, PurchaseStatus status, boolean active, List<PurchaseLine> lines) {
        this.id = id;
        this.supplierId = supplierId;
        this.date = date;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
        this.evidenceUrl = evidenceUrl;
        this.status = status;
        this.active = active;
        this.lines = List.copyOf(lines);
    }

    public static Purchase create(Long supplierId, LocalDateTime date, BigDecimal subtotal, BigDecimal taxTotal,
            BigDecimal total, String evidenceUrl, List<PurchaseLine> lines) {
        return new Purchase(null, requireLong(supplierId, "supplierId"), requireDate(date), requireNonNegative(subtotal,
                "subtotal"), requireNonNegative(taxTotal, "taxTotal"), requireNonNegative(total, "total"),
                normalizeOptional(evidenceUrl), PurchaseStatus.PENDING, true, requireLines(lines));
    }

    public static Purchase restore(Long id, Long supplierId, LocalDateTime date, BigDecimal subtotal,
            BigDecimal taxTotal, BigDecimal total, String evidenceUrl, PurchaseStatus status, boolean active,
            List<PurchaseLine> lines) {
        Objects.requireNonNull(id, "id is required");
        return new Purchase(id, requireLong(supplierId, "supplierId"), requireDate(date),
                requireNonNegative(subtotal, "subtotal"), requireNonNegative(taxTotal, "taxTotal"),
                requireNonNegative(total, "total"), normalizeOptional(evidenceUrl), Objects.requireNonNull(status),
                active, requireLines(lines));
    }

    public Purchase replacePending(Long supplierId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
            String evidenceUrl, List<PurchaseLine> lines) {
        if (status != PurchaseStatus.PENDING) {
            throw new IllegalStateException("purchase is not pending");
        }
        return new Purchase(id, requireLong(supplierId, "supplierId"), date, requireNonNegative(subtotal, "subtotal"),
                requireNonNegative(taxTotal, "taxTotal"), requireNonNegative(total, "total"),
                normalizeOptional(evidenceUrl), status, active, requireLines(lines));
    }

    public Purchase markProcessed() {
        return new Purchase(id, supplierId, date, subtotal, taxTotal, total, evidenceUrl, PurchaseStatus.PROCESSED,
                active, lines);
    }

    public Long id() {
        return id;
    }

    public Long supplierId() {
        return supplierId;
    }

    public LocalDateTime date() {
        return date;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal taxTotal() {
        return taxTotal;
    }

    public BigDecimal total() {
        return total;
    }

    public String evidenceUrl() {
        return evidenceUrl;
    }

    public PurchaseStatus status() {
        return status;
    }

    public boolean active() {
        return active;
    }

    public List<PurchaseLine> lines() {
        return lines;
    }

    private static Long requireLong(Long value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " is required");
    }

    private static LocalDateTime requireDate(LocalDateTime value) {
        return Objects.requireNonNull(value, "date is required");
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or positive");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static List<PurchaseLine> requireLines(List<PurchaseLine> lines) {
        Objects.requireNonNull(lines, "lines are required");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
        return lines;
    }
}
