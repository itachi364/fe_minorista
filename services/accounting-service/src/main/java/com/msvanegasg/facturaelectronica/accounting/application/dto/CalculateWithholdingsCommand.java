package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

public record CalculateWithholdingsCommand(
        UUID companyId,
        FiscalOperationType operationType,
        UUID thirdPartyId,
        String conceptCode,
        LocalDate operationDate,
        BigDecimal taxableBaseAmount,
        BigDecimal taxAmount,
        String municipalityCode,
        AccountingSourceType sourceType,
        UUID sourceId,
        CompanyTaxProfileCommand companyProfile,
        ThirdPartyFiscalProfileCommand thirdPartyProfile,
        BigDecimal previousAccumulatedTaxableBase,
        BigDecimal previousAccumulatedTaxAmount,
        Map<com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType, BigDecimal> previousWithheldByType) {

    public CalculateWithholdingsCommand(UUID companyId, FiscalOperationType operationType, UUID thirdPartyId,
            String conceptCode, LocalDate operationDate, BigDecimal taxableBaseAmount, BigDecimal taxAmount,
            String municipalityCode, AccountingSourceType sourceType, UUID sourceId,
            CompanyTaxProfileCommand companyProfile, ThirdPartyFiscalProfileCommand thirdPartyProfile) {
        this(companyId, operationType, thirdPartyId, conceptCode, operationDate, taxableBaseAmount, taxAmount,
                municipalityCode, sourceType, sourceId, companyProfile, thirdPartyProfile, BigDecimal.ZERO,
                BigDecimal.ZERO, Map.of());
    }

    public CalculateWithholdingsCommand {
        previousAccumulatedTaxableBase = previousAccumulatedTaxableBase == null
                ? BigDecimal.ZERO : previousAccumulatedTaxableBase;
        previousAccumulatedTaxAmount = previousAccumulatedTaxAmount == null
                ? BigDecimal.ZERO : previousAccumulatedTaxAmount;
        previousWithheldByType = previousWithheldByType == null ? Map.of() : Map.copyOf(previousWithheldByType);
    }
}
