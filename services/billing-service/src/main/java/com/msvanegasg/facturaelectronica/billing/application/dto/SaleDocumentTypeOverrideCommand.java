package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record SaleDocumentTypeOverrideCommand(UUID companyId, UUID saleId, ElectronicDocumentType documentType,
        UUID authorizedBy, String pin, String reason, String authorizationHeader) {
}
