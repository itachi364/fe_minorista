package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record InvoicingObligationInput(
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
        UUID rutAssetId) {

    public InvoicingObligationInput {
        rutResponsibilityCodes = rutResponsibilityCodes == null ? Set.of() : Set.copyOf(rutResponsibilityCodes);
        ciiuCodes = ciiuCodes == null ? Set.of() : Set.copyOf(ciiuCodes);
        economicOperationTypes = economicOperationTypes == null ? Set.of() : Set.copyOf(economicOperationTypes);
    }
}
