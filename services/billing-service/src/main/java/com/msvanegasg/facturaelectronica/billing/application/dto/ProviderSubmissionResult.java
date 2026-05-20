package com.msvanegasg.facturaelectronica.billing.application.dto;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;

public record ProviderSubmissionResult(ProviderStatus status, String trackingId, String cufeCude, String qrContent,
        String errorCode, String errorMessage) {
}
