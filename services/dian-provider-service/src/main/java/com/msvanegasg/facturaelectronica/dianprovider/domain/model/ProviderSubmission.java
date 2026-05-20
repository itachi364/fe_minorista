package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ProviderSubmission(
        UUID id,
        UUID companyId,
        UUID documentId,
        ProviderDocumentType documentType,
        String idempotencyKey,
        String trackingId,
        ProviderSubmissionStatus status,
        String cufeCude,
        String qrContent,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        String rawRequest,
        String rawResponse) {

    public ProviderSubmission {
        if (id == null || companyId == null || documentId == null || documentType == null || status == null
                || createdAt == null) {
            throw new IllegalArgumentException("La solicitud del proveedor tiene campos obligatorios incompletos.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("La clave de idempotencia es obligatoria.");
        }
        if (trackingId == null || trackingId.isBlank()) {
            throw new IllegalArgumentException("El tracking ID es obligatorio.");
        }
    }
}
