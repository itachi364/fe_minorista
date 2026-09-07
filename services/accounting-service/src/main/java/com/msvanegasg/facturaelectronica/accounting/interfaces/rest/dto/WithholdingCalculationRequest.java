package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WithholdingCalculationRequest(
        @NotNull FiscalOperationType operationType,
        @NotNull UUID thirdPartyId,
        String conceptCode,
        @NotNull LocalDate operationDate,
        @NotNull @PositiveOrZero BigDecimal taxableBaseAmount,
        @PositiveOrZero BigDecimal taxAmount,
        String municipalityCode,
        AccountingSourceType sourceType,
        UUID sourceId,
        @NotNull @Valid CompanyTaxProfileRequest companyProfile,
        @Valid ThirdPartyFiscalProfileRequest thirdPartyProfile) {
}
