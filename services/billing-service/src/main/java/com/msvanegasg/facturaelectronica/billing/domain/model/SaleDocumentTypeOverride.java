package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SaleDocumentTypeOverride(UUID id, UUID companyId, UUID saleId, ElectronicDocumentType documentType,
        UUID authorizedBy, String reason, boolean active, Instant createdAt) {

    public SaleDocumentTypeOverride {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(saleId, "saleId is required");
        Objects.requireNonNull(documentType, "documentType is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        if (!documentType.isSaleDocument()) {
            throw new IllegalArgumentException("documentType must be a sale document type");
        }
    }

    public static SaleDocumentTypeOverride create(UUID id, UUID companyId, UUID saleId,
            ElectronicDocumentType documentType, UUID authorizedBy, String reason, Instant createdAt) {
        return new SaleDocumentTypeOverride(id, companyId, saleId, documentType, authorizedBy,
                reason == null ? "" : reason.trim(), true, createdAt);
    }
}
