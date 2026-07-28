package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentKind;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FiscalNoteRequest(@NotNull UUID originalDocumentId, PosAdjustmentKind adjustmentKind,
        @NotBlank String reason, @NotNull BigDecimal subtotal, @NotNull BigDecimal taxTotal,
        @NotNull BigDecimal total) {
}