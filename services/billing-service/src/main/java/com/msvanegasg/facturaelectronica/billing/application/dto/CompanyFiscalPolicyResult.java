package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CompanyFiscalPolicyResult(UUID companyId, ElectronicDocumentType defaultSaleDocumentType,
        boolean allowDocumentTypeOverride, boolean requirePinForOverride, Instant updatedAt) {
}
