package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentType;

public record PosAdjustmentNoteResult(
        UUID id,
        UUID companyId,
        UUID referencedDocumentId,
        PosAdjustmentType adjustmentType,
        String reason,
        String prefix,
        long number,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        ElectronicDocumentStatus status,
        UUID createdBy,
        Instant createdAt) {
}
