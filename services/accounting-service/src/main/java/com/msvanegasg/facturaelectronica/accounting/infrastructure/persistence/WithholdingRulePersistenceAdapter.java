package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.WithholdingRuleJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.WithholdingRuleJpaRepository;

@Component
public class WithholdingRulePersistenceAdapter implements WithholdingRuleRepositoryPort {

    private final WithholdingRuleJpaRepository repository;

    public WithholdingRulePersistenceAdapter(WithholdingRuleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WithholdingRule> findActiveRules(UUID companyId, FiscalOperationType operationType,
            LocalDate operationDate) {
        return repository.findActiveRules(companyId, operationType, operationDate).stream()
                .map(WithholdingRulePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<WithholdingRule> findAll() {
        return repository.findAll().stream().map(WithholdingRulePersistenceAdapter::toDomain).toList();
    }

    @Override
    public java.util.Optional<WithholdingRule> findById(UUID id) {
        return repository.findById(id).map(WithholdingRulePersistenceAdapter::toDomain);
    }

    @Override
    public WithholdingRule save(WithholdingRule rule) {
        return toDomain(repository.save(toEntity(rule)));
    }

    private static WithholdingRuleJpaEntity toEntity(WithholdingRule rule) {
        WithholdingRuleJpaEntity entity = new WithholdingRuleJpaEntity();
        entity.setId(rule.id());
        entity.setCompanyId(rule.companyId());
        entity.setRuleSetVersion(rule.ruleSetVersion());
        entity.setOperationType(rule.operationType());
        entity.setConceptCode(rule.conceptCode());
        entity.setWithholdingType(rule.withholdingType());
        entity.setBaseMinAmount(rule.baseMinAmount());
        entity.setRate(rule.rate());
        entity.setRequiresCompanyWithholdingAgent(rule.requiresCompanyWithholdingAgent());
        entity.setRequiresCompanyVatResponsible(rule.requiresCompanyVatResponsible());
        entity.setRequiredThirdPartyTaxRegime(rule.requiredThirdPartyTaxRegime());
        entity.setRequiredThirdPartyResponsibility(rule.requiredThirdPartyResponsibility());
        entity.setMunicipalityCode(rule.municipalityCode());
        entity.setCiiuCode(rule.ciiuCode());
        entity.setValidFrom(rule.validFrom());
        entity.setValidTo(rule.validTo());
        entity.setPriority(rule.priority());
        entity.setActive(rule.active());
        entity.setThresholdUnit(rule.thresholdUnit());
        entity.setThresholdValue(rule.thresholdValue());
        entity.setThresholdOperator(rule.thresholdOperator());
        entity.setCalculationBase(rule.calculationBase());
        entity.setThresholdTreatment(rule.thresholdTreatment());
        entity.setDecision(rule.decision());
        entity.setRequiresCompanyVatWithholdingAgent(rule.requiresCompanyVatWithholdingAgent());
        entity.setRequiresCompanyIcaWithholdingAgent(rule.requiresCompanyIcaWithholdingAgent());
        entity.setLegalReference(rule.legalReference());
        entity.setSourceUrl(rule.sourceUrl());
        entity.setSpecificity(rule.specificity());
        entity.setPublished(rule.published());
        entity.setTargetThirdPartyId(rule.targetThirdPartyId());
        entity.setEvidenceReference(rule.evidenceReference());
        return entity;
    }

    private static WithholdingRule toDomain(WithholdingRuleJpaEntity entity) {
        return new WithholdingRule(entity.getId(), entity.getCompanyId(), entity.getRuleSetVersion(),
                entity.getOperationType(), entity.getConceptCode(), entity.getWithholdingType(),
                entity.getBaseMinAmount(), entity.getRate(),
                Boolean.TRUE.equals(entity.getRequiresCompanyWithholdingAgent()),
                Boolean.TRUE.equals(entity.getRequiresCompanyVatResponsible()),
                entity.getRequiredThirdPartyTaxRegime(), entity.getRequiredThirdPartyResponsibility(),
                entity.getMunicipalityCode(), entity.getCiiuCode(), entity.getValidFrom(), entity.getValidTo(),
                entity.getPriority(), Boolean.TRUE.equals(entity.getActive()), entity.getThresholdUnit(),
                entity.getThresholdValue(), entity.getThresholdOperator(), entity.getCalculationBase(),
                entity.getThresholdTreatment(), entity.getDecision(),
                Boolean.TRUE.equals(entity.getRequiresCompanyVatWithholdingAgent()),
                Boolean.TRUE.equals(entity.getRequiresCompanyIcaWithholdingAgent()), entity.getLegalReference(),
                entity.getSourceUrl(), entity.getSpecificity(), Boolean.TRUE.equals(entity.getPublished()),
                entity.getTargetThirdPartyId(), entity.getEvidenceReference());
    }
}
