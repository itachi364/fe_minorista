package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateWithholdingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdTreatment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

class FiscalCatalogManagementServiceTest {
    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void createsCompanyRuleAndRejectsOverlappingEquivalentVersion() {
        Repository repository = new Repository();
        FiscalCatalogManagementService service = service(repository);

        WithholdingRule created = service.create(command(COMPANY_ID));

        assertThat(created.companyId()).isEqualTo(COMPANY_ID);
        assertThat(created.legalReference()).isEqualTo("Acuerdo municipal 2026");
        assertThatThrownBy(() -> service.create(command(COMPANY_ID)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("equivalente");
    }

    @Test
    void deactivationCannotCrossCompanyBoundary() {
        Repository repository = new Repository();
        FiscalCatalogManagementService service = service(repository);
        WithholdingRule created = service.create(command(COMPANY_ID));

        assertThatThrownBy(() -> service.deactivate(UUID.randomUUID(), created.id()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("alcance");
        assertThat(service.deactivate(COMPANY_ID, created.id()).active()).isFalse();
    }

    @Test
    void specificThirdPartyExemptionRequiresDocumentaryEvidence() {
        Repository repository = new Repository();
        FiscalCatalogManagementService service = service(repository);
        CreateWithholdingRuleCommand base = command(COMPANY_ID);
        CreateWithholdingRuleCommand invalid = new CreateWithholdingRuleCommand(base.companyId(),
                base.ruleSetVersion(), base.operationType(), base.conceptCode(), base.withholdingType(),
                BigDecimal.ZERO, base.thresholdUnit(), base.thresholdValue(), base.thresholdOperator(),
                base.calculationBase(), base.thresholdTreatment(), WithholdingDecision.EXEMPT,
                base.requiresCompanyWithholdingAgent(), base.requiresCompanyVatResponsible(),
                base.requiresCompanyVatWithholdingAgent(), base.requiresCompanyIcaWithholdingAgent(),
                base.requiredThirdPartyTaxRegime(), base.requiredThirdPartyResponsibility(), base.municipalityCode(),
                base.ciiuCode(), base.validFrom(), base.validTo(), base.priority(), base.specificity(),
                base.legalReference(), base.sourceUrl(), true, UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.create(invalid)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("evidenceReference");
    }

    @Test
    void specificThirdPartyExemptionRequiresEvidenceFromSameCompany() {
        Repository repository = new Repository();
        FiscalCatalogManagementService service = service(repository);
        CreateWithholdingRuleCommand base = command(COMPANY_ID);
        CreateWithholdingRuleCommand invalid = new CreateWithholdingRuleCommand(base.companyId(),
                base.ruleSetVersion(), base.operationType(), base.conceptCode(), base.withholdingType(),
                BigDecimal.ZERO, base.thresholdUnit(), base.thresholdValue(), base.thresholdOperator(),
                base.calculationBase(), base.thresholdTreatment(), WithholdingDecision.EXEMPT,
                base.requiresCompanyWithholdingAgent(), base.requiresCompanyVatResponsible(),
                base.requiresCompanyVatWithholdingAgent(), base.requiresCompanyIcaWithholdingAgent(),
                base.requiredThirdPartyTaxRegime(), base.requiredThirdPartyResponsibility(), base.municipalityCode(),
                base.ciiuCode(), base.validFrom(), base.validTo(), base.priority(), base.specificity(),
                base.legalReference(), base.sourceUrl(), true, UUID.randomUUID(),
                "/api/v1/companies/99999999-9999-9999-9999-999999999999/files/asset-id");

        assertThatThrownBy(() -> service.create(invalid)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rule company");
    }

    @Test
    void globalRuleRejectsCompanyEvidence() {
        Repository repository = new Repository();
        FiscalCatalogManagementService service = service(repository);
        CreateWithholdingRuleCommand base = command(null);
        CreateWithholdingRuleCommand invalid = new CreateWithholdingRuleCommand(base.companyId(),
                base.ruleSetVersion(), base.operationType(), base.conceptCode(), base.withholdingType(),
                BigDecimal.ZERO, base.thresholdUnit(), base.thresholdValue(), base.thresholdOperator(),
                base.calculationBase(), base.thresholdTreatment(), WithholdingDecision.EXEMPT,
                base.requiresCompanyWithholdingAgent(), base.requiresCompanyVatResponsible(),
                base.requiresCompanyVatWithholdingAgent(), base.requiresCompanyIcaWithholdingAgent(),
                base.requiredThirdPartyTaxRegime(), base.requiredThirdPartyResponsibility(), base.municipalityCode(),
                base.ciiuCode(), base.validFrom(), base.validTo(), base.priority(), base.specificity(),
                base.legalReference(), base.sourceUrl(), true, UUID.randomUUID(), "/api/v1/companies/any/files/asset");

        assertThatThrownBy(() -> service.create(invalid)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("global rule");
    }

    private static FiscalCatalogManagementService service(Repository repository) {
        FiscalParameterRepositoryPort parameters = new FiscalParameterRepositoryPort() {
            public Optional<FiscalParameter> findEffective(String code, LocalDate date) { return Optional.empty(); }
            public List<FiscalParameter> findAll() { return List.of(); }
        };
        return new FiscalCatalogManagementService(repository, parameters, UUID::randomUUID);
    }

    private static CreateWithholdingRuleCommand command(UUID companyId) {
        return new CreateWithholdingRuleCommand(companyId, "BOG-2026-01", FiscalOperationType.PURCHASE, "SERVICE",
                WithholdingType.RETEICA, new BigDecimal("0.00966"), FiscalThresholdUnit.COP, BigDecimal.ZERO,
                FiscalThresholdOperator.GTE, FiscalCalculationBase.TAXABLE_BASE,
                FiscalThresholdTreatment.FULL_AMOUNT, WithholdingDecision.APPLIED, false, false, false, true,
                null, null, "11001", "6201", LocalDate.of(2026, 1, 1), null, 100, 20,
                "Acuerdo municipal 2026", "https://example.test/acuerdo", true, null, null);
    }

    private static final class Repository implements WithholdingRuleRepositoryPort {
        private final List<WithholdingRule> rules = new ArrayList<>();
        public List<WithholdingRule> findActiveRules(UUID companyId, FiscalOperationType type, LocalDate date) {
            return rules;
        }
        public List<WithholdingRule> findAll() { return List.copyOf(rules); }
        public Optional<WithholdingRule> findById(UUID id) { return rules.stream().filter(rule -> rule.id().equals(id)).findFirst(); }
        public WithholdingRule save(WithholdingRule rule) {
            rules.removeIf(existing -> existing.id().equals(rule.id()));
            rules.add(rule);
            return rule;
        }
    }
}
