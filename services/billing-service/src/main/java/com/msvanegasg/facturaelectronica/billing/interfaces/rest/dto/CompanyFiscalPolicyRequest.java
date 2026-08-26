package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

import jakarta.validation.constraints.NotNull;

public record CompanyFiscalPolicyRequest(@NotNull ElectronicDocumentType defaultSaleDocumentType,
        boolean allowDocumentTypeOverride, boolean requirePinForOverride) {
}
