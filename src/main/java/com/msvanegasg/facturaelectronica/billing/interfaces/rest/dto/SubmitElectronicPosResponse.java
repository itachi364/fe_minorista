package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public record SubmitElectronicPosResponse(
        UUID documentId,
        String providerSubmissionId,
        ProviderSubmissionStatus providerStatus,
        ElectronicDocumentStatus documentStatus,
        String cufeCude,
        String qrContent,
        String errorCode,
        String errorMessage) {
}
