package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;

public record SubmitProviderDocumentCommand(
        UUID companyId,
        UUID documentId,
        ProviderDocumentType documentType,
        String idempotencyKey,
        String payload) {
}
