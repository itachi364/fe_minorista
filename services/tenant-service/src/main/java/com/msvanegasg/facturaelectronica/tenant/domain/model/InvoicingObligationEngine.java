package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class InvoicingObligationEngine {
    private static final Set<String> ALLOWED_SPECIAL_EXCEPTIONS = Set.of(
            "PUBLIC_URBAN_TRANSPORT",
            "FINANCIAL_ENTITY_OPERATION",
            "EMPLOYMENT_OR_PENSION_INCOME",
            "FOREIGN_DIGITAL_SERVICE");
    private static final Set<String> ALLOWED_EXCEPTION_SCOPES = Set.of(
            "ONLY_DECLARED_OPERATION",
            "ALL_OPERATIONS_BY_SUBJECT");

    public InvoicingObligationDecision evaluate(InvoicingObligationInput input, BigDecimal uvtValue) {
        if (input == null || uvtValue == null || uvtValue.signum() <= 0) {
            return review("REQUIRED_DATA_MISSING", "UVT_OR_INPUT_MISSING");
        }
        Set<String> responsibilities = input.rutResponsibilityCodes();
        if (hasCode(responsibilities, "52")) {
            if (Boolean.TRUE.equals(input.voluntaryElectronicInvoicer())) {
                return decision(InvoicingObligationStatus.VOLUNTARY_ELECTRONIC,
                        "VOLUNTARY_ELECTRONIC_INVOICER", "RUT_52", "VOLUNTARY_ENROLLMENT");
            }
            return obligated("ELECTRONIC_INVOICER", "RUT_52");
        }
        if (isSimple(input, responsibilities)) {
            return obligated("SIMPLE_REGIME", "RUT_47");
        }
        if (hasCode(responsibilities, "48") || hasCode(responsibilities, "33")) {
            return obligated("TAX_RESPONSIBILITY", hasCode(responsibilities, "48") ? "RUT_48" : "RUT_33");
        }
        if (missingIdentityOrEvidence(input)) {
            return review("REQUIRED_DATA_MISSING", "RUT_EVIDENCE_OR_IDENTITY_MISSING");
        }
        if ("JURIDICAL".equals(normalize(input.personType()))) {
            if (hasSpecialException(input)) {
                return notObligated("SPECIAL_EXCEPTION_VERIFIED", "SPECIAL_EXCEPTION", input.specialExceptionType());
            }
            return obligated("JURIDICAL_SELLER", "LEGAL_ENTITY_SELLS_GOODS_OR_SERVICES");
        }
        if (!"NATURAL".equals(normalize(input.personType()))) {
            return review("PERSON_TYPE_UNSUPPORTED", "PERSON_TYPE_UNKNOWN");
        }

        BigDecimal threshold = uvtValue.multiply(BigDecimal.valueOf(3500));
        if (hasCode(responsibilities, "49")) {
            return evaluateNaturalNonVat(input, threshold);
        }
        if (hasCode(responsibilities, "50")) {
            return evaluateNaturalNonConsumption(input, threshold);
        }
        if (Boolean.TRUE.equals(input.onlyExcludedOrUntaxedOperations())) {
            if (missing(input.previousYearGrossActivityIncome(), input.currentYearGrossActivityIncome())) {
                return review("EXCLUDED_OPERATIONS_DATA_MISSING", "INCOME_MISSING");
            }
            if (below(input.previousYearGrossActivityIncome(), threshold)
                    && below(input.currentYearGrossActivityIncome(), threshold)) {
                return notObligated("EXCLUDED_OR_UNTAXED_OPERATIONS_VERIFIED", "EXCLUSIVE_EXCLUDED_OPERATIONS",
                        "INCOME_BELOW_3500_UVT");
            }
            return obligated("EXCLUDED_OPERATIONS_THRESHOLD_EXCEEDED", "INCOME_AT_OR_ABOVE_3500_UVT");
        }
        if (hasSpecialException(input)) {
            return notObligated("SPECIAL_EXCEPTION_VERIFIED", "SPECIAL_EXCEPTION", input.specialExceptionType());
        }
        return obligated("GENERAL_SELLER", "NO_VERIFIED_EXCEPTION");
    }

    private InvoicingObligationDecision evaluateNaturalNonVat(InvoicingObligationInput input, BigDecimal threshold) {
        if (input.customsUser() == null || input.establishmentCount() == null || input.exploitsIntangibles() == null
                || missing(input.previousYearGrossActivityIncome(), input.currentYearGrossActivityIncome(),
                        input.previousYearTaxedActivityFinancialOperations(),
                        input.currentYearTaxedActivityFinancialOperations(), input.largestPreviousYearTaxedContract(),
                        input.largestCurrentYearTaxedContract(), input.largestSameCustomerAggregate())) {
            return review("ARTICLE_437_DATA_MISSING", "ARTICLE_437_CONDITION_MISSING");
        }
        List<String> failures = new ArrayList<>();
        addFailure(failures, !below(input.previousYearGrossActivityIncome(), threshold), "PREVIOUS_INCOME_LIMIT");
        addFailure(failures, !below(input.currentYearGrossActivityIncome(), threshold), "CURRENT_INCOME_LIMIT");
        addFailure(failures, input.establishmentCount() > 1, "MORE_THAN_ONE_ESTABLISHMENT");
        addFailure(failures, Boolean.TRUE.equals(input.exploitsIntangibles()), "EXPLOITS_INTANGIBLES");
        addFailure(failures, Boolean.TRUE.equals(input.customsUser()), "CUSTOMS_USER");
        addFailure(failures, !below(input.largestPreviousYearTaxedContract(), threshold), "PREVIOUS_CONTRACT_LIMIT");
        addFailure(failures, !below(input.largestCurrentYearTaxedContract(), threshold), "CURRENT_CONTRACT_LIMIT");
        addFailure(failures, !below(input.largestSameCustomerAggregate(), threshold), "CUSTOMER_AGGREGATE_LIMIT");
        addFailure(failures, above(input.previousYearTaxedActivityFinancialOperations(), threshold),
                "PREVIOUS_FINANCIAL_OPERATIONS_LIMIT");
        addFailure(failures, above(input.currentYearTaxedActivityFinancialOperations(), threshold),
                "CURRENT_FINANCIAL_OPERATIONS_LIMIT");
        if (!failures.isEmpty()) {
            return new InvoicingObligationDecision(InvoicingObligationStatus.OBLIGATED,
                    "ARTICLE_437_CONDITION_FAILED", List.copyOf(failures));
        }
        return notObligated("NATURAL_NON_VAT_RESPONSIBLE_ALL_CONDITIONS_MET", "RUT_49",
                "ARTICLE_437_ALL_CONDITIONS_MET", "INCOME_BELOW_3500_UVT", "SINGLE_ESTABLISHMENT");
    }

    private InvoicingObligationDecision evaluateNaturalNonConsumption(InvoicingObligationInput input,
            BigDecimal threshold) {
        if (input.previousYearGrossActivityIncome() == null || input.establishmentCount() == null) {
            return review("ARTICLE_512_13_DATA_MISSING", "INCOME_OR_ESTABLISHMENT_MISSING");
        }
        if (!below(input.previousYearGrossActivityIncome(), threshold) || input.establishmentCount() > 1) {
            return obligated("ARTICLE_512_13_CONDITION_FAILED", "RUT_50_CONDITIONS_FAILED");
        }
        return notObligated("NATURAL_NON_CONSUMPTION_RESPONSIBLE_CONDITIONS_MET", "RUT_50",
                "INCOME_BELOW_3500_UVT", "SINGLE_ESTABLISHMENT");
    }

    private static boolean missingIdentityOrEvidence(InvoicingObligationInput input) {
        return input.personType() == null || input.personType().isBlank() || input.taxRegime() == null
                || input.taxRegime().isBlank() || input.rutGeneratedAt() == null || input.rutAssetId() == null
                || input.rutResponsibilityCodes().isEmpty();
    }

    private static boolean isSimple(InvoicingObligationInput input, Set<String> responsibilities) {
        return "SIMPLE".equals(normalize(input.taxRegime())) || hasCode(responsibilities, "47");
    }

    private static boolean hasSpecialException(InvoicingObligationInput input) {
        return ALLOWED_SPECIAL_EXCEPTIONS.contains(normalize(input.specialExceptionType()))
                && ALLOWED_EXCEPTION_SCOPES.contains(normalize(input.specialExceptionScope()));
    }

    private static boolean hasCode(Set<String> values, String expected) {
        return values.stream().map(InvoicingObligationEngine::normalize)
                .anyMatch(value -> value.equals(expected) || value.endsWith("-" + expected)
                        || value.startsWith(expected + "-") || value.startsWith("O-" + expected));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean missing(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value == null || value.signum() < 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean below(BigDecimal value, BigDecimal threshold) {
        return value.compareTo(threshold) < 0;
    }

    private static boolean above(BigDecimal value, BigDecimal threshold) {
        return value.compareTo(threshold) > 0;
    }

    private static void addFailure(List<String> failures, boolean failed, String reason) {
        if (failed) {
            failures.add(reason);
        }
    }

    private static InvoicingObligationDecision obligated(String code, String... reasons) {
        return decision(InvoicingObligationStatus.OBLIGATED, code, reasons);
    }

    private static InvoicingObligationDecision notObligated(String code, String... reasons) {
        return decision(InvoicingObligationStatus.NOT_OBLIGATED_VERIFIED, code, reasons);
    }

    private static InvoicingObligationDecision review(String code, String... reasons) {
        return decision(InvoicingObligationStatus.REVIEW_REQUIRED, code, reasons);
    }

    private static InvoicingObligationDecision decision(InvoicingObligationStatus status, String code,
            String... reasons) {
        return new InvoicingObligationDecision(status, code, List.of(reasons));
    }
}
