package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;

public record ProviderSubmissionResult(
        UUID id,
        UUID companyId,
        UUID documentId,
        ProviderDocumentType documentType,
        String trackingId,
        ProviderSubmissionStatus status,
        String cufeCude,
        String qrContent,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        String rawResponse) {
}
