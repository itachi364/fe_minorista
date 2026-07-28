package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record FiscalNote(UUID id, UUID companyId, UUID originalDocumentId, FiscalNoteType noteType,
        PosAdjustmentKind adjustmentKind, ElectronicDocumentStatus status, ProviderStatus providerStatus,
        String reason, String prefix, long documentNumber, String cufeCude, String qrContent, BigDecimal subtotal,
        BigDecimal taxTotal, BigDecimal total, String providerTrackingId, String providerErrorCode,
        String providerErrorMessage, String idempotencyKey, Instant issuedAt) {

    public FiscalNote {
        require(id, "id");
        require(companyId, "companyId");
        require(originalDocumentId, "originalDocumentId");
        require(noteType, "noteType");
        require(status, "status");
        require(providerStatus, "providerStatus");
        require(reason, "reason");
        require(prefix, "prefix");
        require(cufeCude, "cufeCude");
        require(qrContent, "qrContent");
        require(subtotal, "subtotal");
        require(taxTotal, "taxTotal");
        require(total, "total");
        require(idempotencyKey, "idempotencyKey");
        require(issuedAt, "issuedAt");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        if (subtotal.signum() < 0 || taxTotal.signum() < 0 || total.signum() < 0) {
            throw new IllegalArgumentException("note amounts must be positive");
        }
    }

    public ElectronicDocumentType documentType() {
        return switch (noteType) {
            case CREDIT_NOTE -> ElectronicDocumentType.CREDIT_NOTE;
            case DEBIT_NOTE -> ElectronicDocumentType.DEBIT_NOTE;
            case POS_ADJUSTMENT_NOTE -> ElectronicDocumentType.POS_ADJUSTMENT_NOTE;
        };
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}