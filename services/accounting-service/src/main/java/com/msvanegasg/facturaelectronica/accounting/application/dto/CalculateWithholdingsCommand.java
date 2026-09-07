package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        ThirdPartyFiscalProfileCommand thirdPartyProfile) {
}
