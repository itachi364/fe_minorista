package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentKind;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;

public record FiscalNoteResponse(UUID id, UUID companyId, UUID originalDocumentId, FiscalNoteType noteType,
        PosAdjustmentKind adjustmentKind, ElectronicDocumentStatus status, ProviderStatus providerStatus,
        String reason, String prefix, long documentNumber, String cufeCude, String qrContent, BigDecimal subtotal,
        BigDecimal taxTotal, BigDecimal total, String providerTrackingId, String providerErrorCode,
        String providerErrorMessage, Instant issuedAt) {
}