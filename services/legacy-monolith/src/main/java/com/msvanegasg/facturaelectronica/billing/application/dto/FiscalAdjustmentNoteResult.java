package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record FiscalAdjustmentNoteResult(
        UUID id,
        UUID companyId,
        UUID referencedDocumentId,
        ElectronicDocumentType documentType,
        String reason,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        ElectronicDocumentStatus status,
        UUID createdBy,
        Instant createdAt) {
}
