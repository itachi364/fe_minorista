package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalAccumulationScope;

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
        Map<com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType, BigDecimal> previousWithheldByType,
        BigDecimal aiuAmount,
        BigDecimal grossPaymentAmount,
        UUID contractId,
        Map<FiscalAccumulationScope, FiscalAccumulationTotals> accumulationsByScope) {

    public CalculateWithholdingsCommand(UUID companyId, FiscalOperationType operationType, UUID thirdPartyId,
            String conceptCode, LocalDate operationDate, BigDecimal taxableBaseAmount, BigDecimal taxAmount,
            String municipalityCode, AccountingSourceType sourceType, UUID sourceId,
            CompanyTaxProfileCommand companyProfile, ThirdPartyFiscalProfileCommand thirdPartyProfile) {
        this(companyId, operationType, thirdPartyId, conceptCode, operationDate, taxableBaseAmount, taxAmount,
                municipalityCode, sourceType, sourceId, companyProfile, thirdPartyProfile, BigDecimal.ZERO,
                BigDecimal.ZERO, Map.of(), BigDecimal.ZERO, taxableBaseAmount == null || taxAmount == null
                        ? BigDecimal.ZERO : taxableBaseAmount.add(taxAmount), null, Map.of());
    }

    public CalculateWithholdingsCommand(UUID companyId, FiscalOperationType operationType, UUID thirdPartyId,
            String conceptCode, LocalDate operationDate, BigDecimal taxableBaseAmount, BigDecimal taxAmount,
            String municipalityCode, AccountingSourceType sourceType, UUID sourceId,
            CompanyTaxProfileCommand companyProfile, ThirdPartyFiscalProfileCommand thirdPartyProfile,
            BigDecimal previousAccumulatedTaxableBase, BigDecimal previousAccumulatedTaxAmount,
            Map<com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType, BigDecimal> previousWithheldByType) {
        this(companyId, operationType, thirdPartyId, conceptCode, operationDate, taxableBaseAmount, taxAmount,
                municipalityCode, sourceType, sourceId, companyProfile, thirdPartyProfile,
                previousAccumulatedTaxableBase, previousAccumulatedTaxAmount, previousWithheldByType,
                BigDecimal.ZERO, taxableBaseAmount == null || taxAmount == null ? BigDecimal.ZERO
                        : taxableBaseAmount.add(taxAmount), null, Map.of());
    }

    public CalculateWithholdingsCommand {
        previousAccumulatedTaxableBase = previousAccumulatedTaxableBase == null
                ? BigDecimal.ZERO : previousAccumulatedTaxableBase;
        previousAccumulatedTaxAmount = previousAccumulatedTaxAmount == null
                ? BigDecimal.ZERO : previousAccumulatedTaxAmount;
        previousWithheldByType = previousWithheldByType == null ? Map.of() : Map.copyOf(previousWithheldByType);
        aiuAmount = aiuAmount == null ? BigDecimal.ZERO : aiuAmount;
        grossPaymentAmount = grossPaymentAmount == null ? BigDecimal.ZERO : grossPaymentAmount;
        accumulationsByScope = accumulationsByScope == null ? Map.of() : Map.copyOf(accumulationsByScope);
    }
}
