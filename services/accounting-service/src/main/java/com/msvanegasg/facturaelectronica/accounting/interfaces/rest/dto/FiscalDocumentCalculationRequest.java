package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FiscalDocumentCalculationRequest(
        @NotNull FiscalOperationType operationType,
        @NotNull UUID thirdPartyId,
        @NotNull LocalDate operationDate,
        String municipalityCode,
        AccountingSourceType sourceType,
        UUID sourceId,
        @NotEmpty List<@Valid FiscalDocumentLineRequest> lines) {
}
