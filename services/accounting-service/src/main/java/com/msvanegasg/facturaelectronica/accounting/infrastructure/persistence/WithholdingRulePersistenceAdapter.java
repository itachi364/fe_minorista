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

    private static WithholdingRule toDomain(WithholdingRuleJpaEntity entity) {
        return new WithholdingRule(entity.getId(), entity.getCompanyId(), entity.getRuleSetVersion(),
                entity.getOperationType(), entity.getConceptCode(), entity.getWithholdingType(),
                entity.getBaseMinAmount(), entity.getRate(),
                Boolean.TRUE.equals(entity.getRequiresCompanyWithholdingAgent()),
                Boolean.TRUE.equals(entity.getRequiresCompanyVatResponsible()),
                entity.getRequiredThirdPartyTaxRegime(), entity.getRequiredThirdPartyResponsibility(),
                entity.getMunicipalityCode(), entity.getCiiuCode(), entity.getValidFrom(), entity.getValidTo(),
                entity.getPriority(), Boolean.TRUE.equals(entity.getActive()));
    }
}
