package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CreateFiscalAdjustmentNoteCommand(
        UUID companyId,
        UUID referencedDocumentId,
        ElectronicDocumentType documentType,
        String reason,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        UUID createdBy) {
}
