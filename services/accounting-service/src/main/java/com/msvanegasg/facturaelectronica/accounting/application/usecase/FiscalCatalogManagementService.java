package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateWithholdingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalCatalogUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;

@Transactional
public class FiscalCatalogManagementService implements ManageFiscalCatalogUseCase {
    private final WithholdingRuleRepositoryPort ruleRepository;
    private final FiscalParameterRepositoryPort parameterRepository;
    private final IdGeneratorPort idGenerator;

    public FiscalCatalogManagementService(WithholdingRuleRepositoryPort ruleRepository,
            FiscalParameterRepositoryPort parameterRepository, IdGeneratorPort idGenerator) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.parameterRepository = Objects.requireNonNull(parameterRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiscalParameter> parameters() {
        return parameterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithholdingRule> rules(UUID companyId, Boolean active) {
        return ruleRepository.findAll().stream()
                .filter(rule -> rule.companyId() == null || rule.companyId().equals(companyId))
                .filter(rule -> active == null || rule.active() == active)
                .sorted(Comparator.comparing(WithholdingRule::withholdingType)
                        .thenComparing(WithholdingRule::conceptCode, Comparator.nullsFirst(String::compareTo))
                        .thenComparing(WithholdingRule::validFrom).reversed())
                .toList();
    }

    @Override
    public WithholdingRule create(CreateWithholdingRuleCommand command) {
        validate(command);
        WithholdingRule rule = new WithholdingRule(idGenerator.newId(), command.companyId(),
                command.ruleSetVersion().trim(), command.operationType(), normalize(command.conceptCode()),
                command.withholdingType(), BigDecimal.ZERO, command.rate(), command.requiresCompanyWithholdingAgent(),
                command.requiresCompanyVatResponsible(), normalize(command.requiredThirdPartyTaxRegime()),
                normalize(command.requiredThirdPartyResponsibility()), normalize(command.municipalityCode()),
                normalize(command.ciiuCode()), command.validFrom(), command.validTo(), command.priority(), true,
                command.thresholdUnit(), command.thresholdValue(), command.thresholdOperator(),
                command.calculationBase(), command.thresholdTreatment(), command.decision(),
                command.requiresCompanyVatWithholdingAgent(), command.requiresCompanyIcaWithholdingAgent(),
                command.legalReference().trim(), command.sourceUrl().trim(), command.specificity(), command.published(),
                command.targetThirdPartyId(), normalize(command.evidenceReference()));
        assertNoEquivalentActiveRule(rule);
        return ruleRepository.save(rule);
    }

    @Override
    public WithholdingRule deactivate(UUID companyId, UUID ruleId) {
        WithholdingRule current = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalStateException("La regla fiscal no existe."));
        if (!Objects.equals(current.companyId(), companyId)) {
            throw new IllegalStateException("La regla fiscal no pertenece al alcance autorizado.");
        }
        WithholdingRule inactive = new WithholdingRule(current.id(), current.companyId(), current.ruleSetVersion(),
                current.operationType(), current.conceptCode(), current.withholdingType(), current.baseMinAmount(),
                current.rate(), current.requiresCompanyWithholdingAgent(), current.requiresCompanyVatResponsible(),
                current.requiredThirdPartyTaxRegime(), current.requiredThirdPartyResponsibility(),
                current.municipalityCode(), current.ciiuCode(), current.validFrom(), current.validTo(),
                current.priority(), false, current.thresholdUnit(), current.thresholdValue(),
                current.thresholdOperator(), current.calculationBase(), current.thresholdTreatment(),
                current.decision(), current.requiresCompanyVatWithholdingAgent(),
                current.requiresCompanyIcaWithholdingAgent(), current.legalReference(), current.sourceUrl(),
                current.specificity(), current.published(), current.targetThirdPartyId(), current.evidenceReference());
        return ruleRepository.save(inactive);
    }

    private void assertNoEquivalentActiveRule(WithholdingRule candidate) {
        boolean duplicate = ruleRepository.findAll().stream().anyMatch(existing -> existing.active()
                && Objects.equals(existing.companyId(), candidate.companyId())
                && existing.operationType() == candidate.operationType()
                && existing.withholdingType() == candidate.withholdingType()
                && Objects.equals(existing.conceptCode(), candidate.conceptCode())
                && Objects.equals(existing.municipalityCode(), candidate.municipalityCode())
                && Objects.equals(existing.ciiuCode(), candidate.ciiuCode())
                && Objects.equals(existing.targetThirdPartyId(), candidate.targetThirdPartyId())
                && overlaps(existing, candidate));
        if (duplicate) {
            throw new IllegalStateException("Ya existe una regla fiscal activa equivalente en la vigencia indicada.");
        }
    }

    private static boolean overlaps(WithholdingRule left, WithholdingRule right) {
        return (left.validTo() == null || !left.validTo().isBefore(right.validFrom()))
                && (right.validTo() == null || !right.validTo().isBefore(left.validFrom()));
    }

    private static void validate(CreateWithholdingRuleCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (command.ruleSetVersion() == null || command.ruleSetVersion().isBlank()
                || command.legalReference() == null || command.legalReference().isBlank()
                || command.sourceUrl() == null || command.sourceUrl().isBlank()) {
            throw new IllegalArgumentException("version, legalReference and sourceUrl are required");
        }
        Objects.requireNonNull(command.operationType(), "operationType is required");
        Objects.requireNonNull(command.withholdingType(), "withholdingType is required");
        Objects.requireNonNull(command.rate(), "rate is required");
        Objects.requireNonNull(command.thresholdValue(), "thresholdValue is required");
        Objects.requireNonNull(command.validFrom(), "validFrom is required");
        if (command.rate().signum() < 0 || command.thresholdValue().signum() < 0) {
            throw new IllegalArgumentException("rate and thresholdValue cannot be negative");
        }
        if (command.targetThirdPartyId() != null
                && (command.decision() != com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision.EXEMPT
                        || command.evidenceReference() == null || command.evidenceReference().isBlank())) {
            throw new IllegalArgumentException(
                    "a third-party exemption requires EXEMPT decision and evidenceReference");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
