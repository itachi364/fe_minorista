package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CompanyFiscalPolicyResponse(UUID companyId, ElectronicDocumentType defaultSaleDocumentType,
        boolean allowDocumentTypeOverride, boolean requirePinForOverride, Instant updatedAt) {
}
