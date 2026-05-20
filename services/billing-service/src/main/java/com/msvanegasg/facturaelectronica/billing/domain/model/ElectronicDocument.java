package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ElectronicDocument(UUID id, UUID companyId, UUID saleId, ElectronicDocumentType documentType,
        ElectronicDocumentStatus status, ProviderStatus providerStatus, String prefix, long documentNumber,
        String cufeCude, String qrContent, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
        String providerTrackingId, String providerErrorCode, String providerErrorMessage, String idempotencyKey,
        Instant issuedAt, Instant inventoryAppliedAt, Instant accountingAppliedAt) {

    public ElectronicDocument {
        require(id, "id");
        require(companyId, "companyId");
        require(saleId, "saleId");
        require(documentType, "documentType");
        require(status, "status");
        require(providerStatus, "providerStatus");
        require(prefix, "prefix");
        require(cufeCude, "cufeCude");
        require(qrContent, "qrContent");
        require(subtotal, "subtotal");
        require(taxTotal, "taxTotal");
        require(total, "total");
        require(idempotencyKey, "idempotencyKey");
        require(issuedAt, "issuedAt");
    }

    public boolean inventoryApplied() {
        return inventoryAppliedAt != null;
    }

    public boolean accountingApplied() {
        return accountingAppliedAt != null;
    }

    public ElectronicDocument markInventoryApplied(Instant appliedAt) {
        require(appliedAt, "appliedAt");
        return new ElectronicDocument(id, companyId, saleId, documentType, status, providerStatus, prefix,
                documentNumber, cufeCude, qrContent, subtotal, taxTotal, total, providerTrackingId, providerErrorCode,
                providerErrorMessage, idempotencyKey, issuedAt, appliedAt, accountingAppliedAt);
    }

    public ElectronicDocument markAccountingApplied(Instant appliedAt) {
        require(appliedAt, "appliedAt");
        return new ElectronicDocument(id, companyId, saleId, documentType, status, providerStatus, prefix,
                documentNumber, cufeCude, qrContent, subtotal, taxTotal, total, providerTrackingId, providerErrorCode,
                providerErrorMessage, idempotencyKey, issuedAt, inventoryAppliedAt, appliedAt);
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
