package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentType;

public record CreatePosAdjustmentNoteCommand(
        UUID companyId,
        UUID referencedDocumentId,
        PosAdjustmentType adjustmentType,
        String reason,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        LocalDate documentDate,
        FiscalEnvironment environment,
        UUID createdBy) {
}
