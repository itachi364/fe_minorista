package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentKind;

public record CreateFiscalNoteCommand(UUID companyId, UUID originalDocumentId, FiscalNoteType noteType,
        PosAdjustmentKind adjustmentKind, String reason, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
        String idempotencyKey) {
}