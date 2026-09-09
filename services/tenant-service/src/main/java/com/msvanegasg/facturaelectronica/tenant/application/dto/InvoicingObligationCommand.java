package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record InvoicingObligationCommand(
        String personType,
        String taxRegime,
        LocalDate rutGeneratedAt,
        Set<String> rutResponsibilityCodes,
        Set<String> ciiuCodes,
        Set<String> economicOperationTypes,
        Boolean customsUser,
        Integer establishmentCount,
        Boolean exploitsIntangibles,
        Boolean onlyExcludedOrUntaxedOperations,
        BigDecimal previousYearGrossActivityIncome,
        BigDecimal currentYearGrossActivityIncome,
        BigDecimal previousYearTaxedActivityFinancialOperations,
        BigDecimal currentYearTaxedActivityFinancialOperations,
        BigDecimal largestPreviousYearTaxedContract,
        BigDecimal largestCurrentYearTaxedContract,
        BigDecimal largestSameCustomerAggregate,
        Boolean voluntaryElectronicInvoicer,
        String specialExceptionType,
        String specialExceptionScope,
        UUID rutAssetId,
        UUID evaluatedBy) {
}
