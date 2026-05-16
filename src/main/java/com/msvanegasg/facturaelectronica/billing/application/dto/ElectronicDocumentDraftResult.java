package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record ElectronicDocumentDraftResult(
        UUID id,
        UUID companyId,
        ElectronicDocumentType documentType,
        ElectronicDocumentStatus status,
        Instant createdAt) {
}
