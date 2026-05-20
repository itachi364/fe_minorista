package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PosAdjustmentNote {

    private final UUID id;
    private final UUID companyId;
    private final UUID referencedDocumentId;
    private final PosAdjustmentType adjustmentType;
    private final String reason;
    private final String prefix;
    private final long number;
    private final BigDecimal subtotal;
    private final BigDecimal taxTotal;
    private final BigDecimal total;
    private final ElectronicDocumentStatus status;
    private final UUID createdBy;
    private final Instant createdAt;

    private PosAdjustmentNote(
            UUID id,
            UUID companyId,
            UUID referencedDocumentId,
            PosAdjustmentType adjustmentType,
            String reason,
            String prefix,
            long number,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            ElectronicDocumentStatus status,
            UUID createdBy,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.referencedDocumentId = referencedDocumentId;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
        this.prefix = prefix;
        this.number = number;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static PosAdjustmentNote create(
            UUID id,
            ElectronicPosDocument referencedDocument,
            PosAdjustmentType adjustmentType,
            String reason,
            FiscalNumberAssignment fiscalNumber,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            UUID createdBy,
            Instant createdAt) {
        requireNonNull(id, "id");
        requireNonNull(referencedDocument, "referencedDocument");
        requireNonNull(adjustmentType, "adjustmentType");
        requireNonNull(fiscalNumber, "fiscalNumber");
        requireNonNull(createdAt, "createdAt");
        requireNonBlank(reason, "reason");
        requireNonNegative(subtotal, "subtotal");
        requireNonNegative(taxTotal, "taxTotal");
        requirePositive(total, "total");
        requireTotalMatches(subtotal, taxTotal, total);
        requireDifferentFiscalNumber(referencedDocument, fiscalNumber);

        return new PosAdjustmentNote(
                id,
                referencedDocument.companyId(),
                referencedDocument.id(),
                adjustmentType,
                reason.trim(),
                fiscalNumber.prefix(),
                fiscalNumber.number(),
                subtotal,
                taxTotal,
                total,
                ElectronicDocumentStatus.NUMBER_ASSIGNED,
                createdBy,
                createdAt);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public UUID referencedDocumentId() {
        return referencedDocumentId;
    }

    public PosAdjustmentType adjustmentType() {
        return adjustmentType;
    }

    public String reason() {
        return reason;
    }

    public String prefix() {
        return prefix;
    }

    public long number() {
        return number;
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

    public ElectronicDocumentStatus status() {
        return status;
    }

    public UUID createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static void requireDifferentFiscalNumber(
            ElectronicPosDocument referencedDocument,
            FiscalNumberAssignment fiscalNumber) {
        if (referencedDocument.prefix().equals(fiscalNumber.prefix())
                && referencedDocument.number() == fiscalNumber.number()) {
            throw new IllegalArgumentException("adjustment note must not reuse referenced POS fiscal number");
        }
    }

    private static void requireTotalMatches(BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {
        if (subtotal.add(taxTotal).compareTo(total) != 0) {
            throw new IllegalArgumentException("total must be equal to subtotal plus taxTotal");
        }
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

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
