package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class FiscalAdjustmentNote {

    private final UUID id;
    private final UUID companyId;
    private final UUID referencedDocumentId;
    private final ElectronicDocumentType documentType;
    private final String reason;
    private final BigDecimal subtotal;
    private final BigDecimal taxTotal;
    private final BigDecimal total;
    private final ElectronicDocumentStatus status;
    private final UUID createdBy;
    private final Instant createdAt;

    private FiscalAdjustmentNote(
            UUID id,
            UUID companyId,
            UUID referencedDocumentId,
            ElectronicDocumentType documentType,
            String reason,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            ElectronicDocumentStatus status,
            UUID createdBy,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.referencedDocumentId = referencedDocumentId;
        this.documentType = documentType;
        this.reason = reason;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static FiscalAdjustmentNote create(
            UUID id,
            ElectronicDocumentLifecycle referencedDocument,
            ElectronicDocumentType documentType,
            String reason,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            UUID createdBy,
            Instant createdAt) {
        requireNonNull(id, "id");
        requireNonNull(referencedDocument, "referencedDocument");
        requireNonNull(documentType, "documentType");
        requireNonNull(createdAt, "createdAt");
        requireNoteType(documentType);
        requireReferencedValidatedInvoice(referencedDocument);
        requireNonBlank(reason, "reason");
        requireNonNegative(subtotal, "subtotal");
        requireNonNegative(taxTotal, "taxTotal");
        requirePositive(total, "total");
        requireTotalMatches(subtotal, taxTotal, total);

        return new FiscalAdjustmentNote(
                id,
                referencedDocument.companyId(),
                referencedDocument.id(),
                documentType,
                reason.trim(),
                subtotal,
                taxTotal,
                total,
                ElectronicDocumentStatus.DRAFT,
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

    public ElectronicDocumentType documentType() {
        return documentType;
    }

    public String reason() {
        return reason;
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

    private static void requireReferencedValidatedInvoice(ElectronicDocumentLifecycle referencedDocument) {
        if (referencedDocument.documentType() != ElectronicDocumentType.ELECTRONIC_INVOICE) {
            throw new IllegalArgumentException("referenced document must be an electronic invoice");
        }
        if (referencedDocument.status() != ElectronicDocumentStatus.VALIDATED) {
            throw new IllegalStateException("referenced invoice must be validated");
        }
    }

    private static void requireNoteType(ElectronicDocumentType documentType) {
        if (documentType != ElectronicDocumentType.CREDIT_NOTE && documentType != ElectronicDocumentType.DEBIT_NOTE) {
            throw new IllegalArgumentException("documentType must be CREDIT_NOTE or DEBIT_NOTE");
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
