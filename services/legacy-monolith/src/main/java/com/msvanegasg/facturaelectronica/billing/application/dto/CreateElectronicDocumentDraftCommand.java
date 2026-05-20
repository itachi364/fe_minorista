package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CreateElectronicDocumentDraftCommand(
        UUID companyId,
        ElectronicDocumentType documentType,
        String idempotencyKey) {
}
