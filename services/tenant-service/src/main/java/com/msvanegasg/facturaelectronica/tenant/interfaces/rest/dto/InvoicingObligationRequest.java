package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record InvoicingObligationRequest(
        @Size(max = 30) String personType,
        @Size(max = 60) String taxRegime,
        LocalDate rutGeneratedAt,
        Set<@Size(max = 30) String> rutResponsibilityCodes,
        Set<@Size(max = 10) String> ciiuCodes,
        Set<@Size(max = 60) String> economicOperationTypes,
        Boolean customsUser,
        @Min(0) Integer establishmentCount,
        Boolean exploitsIntangibles,
        Boolean onlyExcludedOrUntaxedOperations,
        @DecimalMin("0") BigDecimal previousYearGrossActivityIncome,
        @DecimalMin("0") BigDecimal currentYearGrossActivityIncome,
        @DecimalMin("0") BigDecimal previousYearTaxedActivityFinancialOperations,
        @DecimalMin("0") BigDecimal currentYearTaxedActivityFinancialOperations,
        @DecimalMin("0") BigDecimal largestPreviousYearTaxedContract,
        @DecimalMin("0") BigDecimal largestCurrentYearTaxedContract,
        @DecimalMin("0") BigDecimal largestSameCustomerAggregate,
        Boolean voluntaryElectronicInvoicer,
        @Size(max = 80) String specialExceptionType,
        @Size(max = 240) String specialExceptionScope,
        UUID rutAssetId) {
}
