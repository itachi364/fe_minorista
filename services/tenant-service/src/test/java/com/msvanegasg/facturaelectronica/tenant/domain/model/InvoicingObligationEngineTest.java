package com.msvanegasg.facturaelectronica.tenant.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class InvoicingObligationEngineTest {
    private static final BigDecimal UVT_2026 = new BigDecimal("52374");
    private static final BigDecimal BELOW_THRESHOLD = new BigDecimal("100000000");
    private static final UUID RUT_ASSET_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private final InvoicingObligationEngine engine = new InvoicingObligationEngine();

    @Test
    void classifiesElectronicInvoicerAsObligated() {
        var decision = engine.evaluate(input("JURIDICAL", "ORDINARIO", Set.of("O-52")), UVT_2026);
        assertThat(decision.status()).isEqualTo(InvoicingObligationStatus.OBLIGATED);
        assertThat(decision.reasons()).contains("RUT_52");
    }

    @Test
    void classifiesVoluntaryElectronicInvoicerSeparately() {
        var base = input("NATURAL", "ORDINARIO", Set.of("52"));
        var decision = engine.evaluate(copy(base, null, null, true), UVT_2026);
        assertThat(decision.status()).isEqualTo(InvoicingObligationStatus.VOLUNTARY_ELECTRONIC);
    }

    @Test
    void simpleAndVatResponsibilityPrevail() {
        assertThat(engine.evaluate(input("NATURAL", "SIMPLE", Set.of("49")), UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
        assertThat(engine.evaluate(input("NATURAL", "ORDINARIO", Set.of("48", "49")), UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
    }

    @Test
    void juridicalSellerIsObligatedWithoutScopedException() {
        assertThat(engine.evaluate(input("JURIDICAL", "ORDINARIO", Set.of("53")), UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
    }

    @Test
    void verifiesNaturalPersonWhenAllArticle437ConditionsAreMet() {
        var decision = engine.evaluate(input("NATURAL", "ORDINARIO", Set.of("O-49")), UVT_2026);
        assertThat(decision.status()).isEqualTo(InvoicingObligationStatus.NOT_OBLIGATED_VERIFIED);
        assertThat(decision.decisionCode()).isEqualTo("NATURAL_NON_VAT_RESPONSIBLE_ALL_CONDITIONS_MET");
    }

    @Test
    void exactIncomeThresholdMakesNaturalPersonObligated() {
        var base = input("NATURAL", "ORDINARIO", Set.of("49"));
        var atThreshold = UVT_2026.multiply(BigDecimal.valueOf(3500));
        var decision = engine.evaluate(copy(base, atThreshold, null, false), UVT_2026);
        assertThat(decision.status()).isEqualTo(InvoicingObligationStatus.OBLIGATED);
        assertThat(decision.reasons()).contains("PREVIOUS_INCOME_LIMIT");
    }

    @Test
    void missingArticle437ConditionRequiresReview() {
        var base = input("NATURAL", "ORDINARIO", Set.of("49"));
        var incomplete = new InvoicingObligationInput(base.personType(), base.taxRegime(), base.rutGeneratedAt(),
                base.rutResponsibilityCodes(), base.ciiuCodes(), base.economicOperationTypes(), null,
                base.establishmentCount(), base.exploitsIntangibles(), base.onlyExcludedOrUntaxedOperations(),
                base.previousYearGrossActivityIncome(), base.currentYearGrossActivityIncome(),
                base.previousYearTaxedActivityFinancialOperations(), base.currentYearTaxedActivityFinancialOperations(),
                base.largestPreviousYearTaxedContract(), base.largestCurrentYearTaxedContract(),
                base.largestSameCustomerAggregate(), false, null, null, base.rutAssetId());
        assertThat(engine.evaluate(incomplete, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.REVIEW_REQUIRED);
    }

    @Test
    void absentRutEvidenceRequiresReviewBeforeGeneralClassification() {
        var base = input("NATURAL", "ORDINARIO", Set.of("49"));
        var withoutEvidence = new InvoicingObligationInput(base.personType(), base.taxRegime(), base.rutGeneratedAt(),
                base.rutResponsibilityCodes(), base.ciiuCodes(), base.economicOperationTypes(), base.customsUser(),
                base.establishmentCount(), base.exploitsIntangibles(), base.onlyExcludedOrUntaxedOperations(),
                base.previousYearGrossActivityIncome(), base.currentYearGrossActivityIncome(),
                base.previousYearTaxedActivityFinancialOperations(), base.currentYearTaxedActivityFinancialOperations(),
                base.largestPreviousYearTaxedContract(), base.largestCurrentYearTaxedContract(),
                base.largestSameCustomerAggregate(), false, null, null, null);
        assertThat(engine.evaluate(withoutEvidence, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.REVIEW_REQUIRED);
    }

    @Test
    void verifiesRestaurantExceptionAndRejectsItsThreshold() {
        var eligible = input("NATURAL", "ORDINARIO", Set.of("50"));
        assertThat(engine.evaluate(eligible, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.NOT_OBLIGATED_VERIFIED);
        var exceeded = copy(eligible, UVT_2026.multiply(BigDecimal.valueOf(3500)), null, false);
        assertThat(engine.evaluate(exceeded, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
    }

    @Test
    void scopedSpecialExceptionCanVerifyLegalEntity() {
        var base = input("JURIDICAL", "ORDINARIO", Set.of("53"));
        var exception = copy(base, null, "PUBLIC_URBAN_TRANSPORT", false);
        assertThat(engine.evaluate(exception, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.NOT_OBLIGATED_VERIFIED);
    }

    @Test
    void arbitrarySpecialExceptionCannotBypassObligation() {
        var base = input("JURIDICAL", "ORDINARIO", Set.of("53"));
        var manipulated = copy(base, null, "USER_SUPPLIED_EXCEPTION", false);

        assertThat(engine.evaluate(manipulated, UVT_2026).status())
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
    }

    @Test
    void reevaluationRecordsTransitionWhenVerifiedCompanyBecomesObligated() {
        assertThat(InvoicingObligationStatus.afterReevaluation(
                InvoicingObligationStatus.NOT_OBLIGATED_VERIFIED, InvoicingObligationStatus.OBLIGATED))
                .isEqualTo(InvoicingObligationStatus.TRANSITION_TO_OBLIGATED);
        assertThat(InvoicingObligationStatus.afterReevaluation(
                InvoicingObligationStatus.REVIEW_REQUIRED, InvoicingObligationStatus.OBLIGATED))
                .isEqualTo(InvoicingObligationStatus.OBLIGATED);
    }

    private static InvoicingObligationInput input(String personType, String regime, Set<String> responsibilities) {
        return new InvoicingObligationInput(personType, regime, LocalDate.of(2026, 9, 1), responsibilities,
                Set.of("4711"), Set.of("TAXED_GOODS_SALE"), false, 1, false, false,
                BELOW_THRESHOLD, BELOW_THRESHOLD, BELOW_THRESHOLD, BELOW_THRESHOLD,
                BELOW_THRESHOLD, BELOW_THRESHOLD, BELOW_THRESHOLD, false, null, null, RUT_ASSET_ID);
    }

    private static InvoicingObligationInput copy(InvoicingObligationInput base, BigDecimal previousIncome,
            String exceptionType, boolean voluntary) {
        return new InvoicingObligationInput(base.personType(), base.taxRegime(), base.rutGeneratedAt(),
                base.rutResponsibilityCodes(), base.ciiuCodes(), base.economicOperationTypes(), base.customsUser(),
                base.establishmentCount(), base.exploitsIntangibles(), base.onlyExcludedOrUntaxedOperations(),
                previousIncome == null ? base.previousYearGrossActivityIncome() : previousIncome,
                base.currentYearGrossActivityIncome(), base.previousYearTaxedActivityFinancialOperations(),
                base.currentYearTaxedActivityFinancialOperations(), base.largestPreviousYearTaxedContract(),
                base.largestCurrentYearTaxedContract(), base.largestSameCustomerAggregate(), voluntary,
                exceptionType, exceptionType == null ? null : "ONLY_DECLARED_OPERATION", base.rutAssetId());
    }
}
