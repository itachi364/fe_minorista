package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.time.LocalDate;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NumberingResolutionRequest(
        @NotNull ElectronicDocumentType documentType,
        @NotBlank String resolutionNumber,
        String prefix,
        @Positive long fromNumber,
        @Positive long toNumber,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTo,
        @NotNull FiscalEnvironment environment) {
}
