package com.msvanegasg.facturaelectronica.billing.application.dto;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public record DianProviderResponse(
        ProviderSubmissionStatus status,
        String providerSubmissionId,
        String cufeCude,
        String qrContent,
        String xmlContent,
        String graphicRepresentationContent,
        String errorCode,
        String errorMessage) {
}
