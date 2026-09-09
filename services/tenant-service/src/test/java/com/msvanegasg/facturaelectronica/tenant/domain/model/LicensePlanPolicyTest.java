package com.msvanegasg.facturaelectronica.tenant.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

class LicensePlanPolicyTest {

    @Test
    void posUsesTheFixedBasicOperationalSelection() {
        LicensePlanPolicy.Selection selection = LicensePlanPolicy.normalize("POS", Set.of(LicenseModule.ACCOUNTING),
                Set.of(LicenseFeature.ACCOUNTING_ADVANCED));

        assertThat(selection.modules()).contains(LicenseModule.BILLING, LicenseModule.INVENTORY,
                LicenseModule.REPORTS);
        assertThat(selection.modules()).doesNotContain(LicenseModule.ACCOUNTING, LicenseModule.PAYROLL);
        assertThat(selection.features()).contains(LicenseFeature.POS_SALES, LicenseFeature.REPORTS_BASIC,
                LicenseFeature.ACCOUNTING_CORE);
        assertThat(selection.features()).doesNotContain(LicenseFeature.SUPPLIERS,
                LicenseFeature.ACCOUNTING_ADVANCED, LicenseFeature.REPORTS_ASYNC);
    }

    @Test
    void fullIncludesEveryStandardFeatureButNoCommercialCustomization() {
        LicensePlanPolicy.Selection selection = LicensePlanPolicy.normalize("FULL", Set.of(), Set.of());

        assertThat(selection.modules()).containsExactlyInAnyOrder(LicenseModule.values());
        assertThat(selection.features()).contains(LicenseFeature.SUPPLIERS, LicenseFeature.PAYROLL,
                LicenseFeature.FISCAL_RULES_ADVANCED, LicenseFeature.REPORTS_ASYNC);
        assertThat(selection.features()).doesNotContain(LicenseFeature.CUSTOM_RULES,
                LicenseFeature.PRIORITY_SUPPORT, LicenseFeature.ACCOUNTANT_PORTAL);
    }

    @Test
    void customRejectsAFeatureWhoseModuleWasNotSelected() {
        assertThatThrownBy(() -> LicensePlanPolicy.normalize("CUSTOM", Set.of(LicenseModule.COMPANY),
                Set.of(LicenseFeature.PAYROLL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires module PAYROLL");
    }
}
