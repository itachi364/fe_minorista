package com.msvanegasg.facturaelectronica.providerretry;

public record ProviderSubmissionOutcome(ProviderStatus status, String trackingId, String cufeCude, String qrContent,
        String errorCode, String errorMessage) {
}