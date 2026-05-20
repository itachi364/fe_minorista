package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;

public record ElectronicDocumentResult(UUID id, UUID companyId, UUID saleId, ElectronicDocumentType documentType,
        ElectronicDocumentStatus status, ProviderStatus providerStatus, String prefix, long documentNumber,
        String cufeCude, String qrContent, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
        String providerTrackingId, String providerErrorCode, String providerErrorMessage, Instant issuedAt,
        Instant inventoryAppliedAt, Instant accountingAppliedAt) {
}
