package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ProviderSubmissionRecord(
        UUID id,
        UUID companyId,
        UUID documentId,
        ElectronicDocumentType documentType,
        String idempotencyKey,
        String requestPayloadHash,
        ProviderSubmissionStatus status,
        String providerSubmissionId,
        String cufeCude,
        String qrContent,
        String xmlContent,
        String graphicRepresentationContent,
        String errorCode,
        String errorMessage,
        Instant submittedAt) {
}
