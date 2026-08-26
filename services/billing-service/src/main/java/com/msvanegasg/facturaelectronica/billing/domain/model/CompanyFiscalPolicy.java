package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CompanyFiscalPolicy(UUID companyId, ElectronicDocumentType defaultSaleDocumentType,
        boolean allowDocumentTypeOverride, boolean requirePinForOverride, Instant updatedAt) {

    public CompanyFiscalPolicy {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(defaultSaleDocumentType, "defaultSaleDocumentType is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static CompanyFiscalPolicy defaults(UUID companyId) {
        return new CompanyFiscalPolicy(companyId, ElectronicDocumentType.ELECTRONIC_INVOICE, true, true,
                Instant.EPOCH);
    }

    public static CompanyFiscalPolicy configure(UUID companyId, ElectronicDocumentType defaultSaleDocumentType,
            boolean allowDocumentTypeOverride, boolean requirePinForOverride, Instant updatedAt) {
        ElectronicDocumentType normalizedDefault = defaultSaleDocumentType == null
                ? ElectronicDocumentType.ELECTRONIC_INVOICE
                : defaultSaleDocumentType;
        if (!normalizedDefault.isSaleDocument()) {
            throw new IllegalArgumentException("defaultSaleDocumentType must be a sale document type");
        }
        return new CompanyFiscalPolicy(companyId, normalizedDefault, allowDocumentTypeOverride,
                requirePinForOverride, updatedAt);
    }

    public ElectronicDocumentType resolveSaleDocumentType() {
        return defaultSaleDocumentType;
    }
}
