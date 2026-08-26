package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaleDocumentTypeOverrideRequest(@NotNull ElectronicDocumentType documentType, UUID authorizedBy,
        @NotBlank String pin, String reason) {
}
