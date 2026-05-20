package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public record ProviderSubmissionResult(
        UUID submissionId,
        UUID documentId,
        ProviderSubmissionStatus status,
        String providerSubmissionId,
        String cufeCude,
        String qrContent,
        String xmlContent,
        String graphicRepresentationContent,
        String errorCode,
        String errorMessage) {
}
