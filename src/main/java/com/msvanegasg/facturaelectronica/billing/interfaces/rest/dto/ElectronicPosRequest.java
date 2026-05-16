package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ElectronicPosRequest(
        UUID saleId,
        String buyerName,
        String buyerDocumentType,
        String buyerDocumentNumber,
        @NotNull LocalDate documentDate,
        @NotNull FiscalEnvironment environment,
        @Valid @NotEmpty List<ElectronicPosLineRequest> lines) {
}
