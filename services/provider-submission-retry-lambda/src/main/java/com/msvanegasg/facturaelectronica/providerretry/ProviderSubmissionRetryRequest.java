package com.msvanegasg.facturaelectronica.providerretry;

import java.util.Objects;
import java.util.UUID;

public record ProviderSubmissionRetryRequest(UUID companyId, UUID saleId, UUID documentId, String documentType,
        String documentIdempotencyKey) {

    public ProviderSubmissionRetryRequest {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(saleId, "saleId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        documentType = requireText(documentType, "documentType");
        documentIdempotencyKey = requireText(documentIdempotencyKey, "documentIdempotencyKey");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}