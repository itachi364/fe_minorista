package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CompanyFiscalPolicyCommand(UUID companyId, ElectronicDocumentType defaultSaleDocumentType,
        boolean allowDocumentTypeOverride, boolean requirePinForOverride) {
}
