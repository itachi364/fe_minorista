package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.WithholdingCalculationSnapshotJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.WithholdingCalculationSnapshotJpaRepository;

@Component
public class WithholdingCalculationSnapshotPersistenceAdapter implements WithholdingCalculationSnapshotRepositoryPort {

    private final WithholdingCalculationSnapshotJpaRepository repository;

    public WithholdingCalculationSnapshotPersistenceAdapter(WithholdingCalculationSnapshotJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(List<WithholdingCalculationSnapshot> snapshots) {
        repository.saveAll(snapshots.stream().map(WithholdingCalculationSnapshotPersistenceAdapter::toEntity).toList());
    }

    @Override
    public List<WithholdingCalculationSnapshot> findBySource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId) {
        return repository.findByCompanyIdAndSourceTypeAndSourceIdOrderByWithholdingType(companyId, sourceType,
                sourceId).stream().map(WithholdingCalculationSnapshotPersistenceAdapter::toDomain).toList();
    }

    private static WithholdingCalculationSnapshotJpaEntity toEntity(WithholdingCalculationSnapshot snapshot) {
        WithholdingCalculationSnapshotJpaEntity entity = new WithholdingCalculationSnapshotJpaEntity();
        entity.setId(snapshot.id());
        entity.setCompanyId(snapshot.companyId());
        entity.setSourceType(snapshot.sourceType());
        entity.setSourceId(snapshot.sourceId());
        entity.setThirdPartyId(snapshot.thirdPartyId());
        entity.setOperationDate(snapshot.operationDate());
        entity.setWithholdingType(snapshot.withholdingType());
        entity.setBaseAmount(snapshot.baseAmount());
        entity.setRate(snapshot.rate());
        entity.setAmount(snapshot.amount());
        entity.setRuleVersion(snapshot.ruleVersion());
        entity.setDecision(snapshot.decision());
        entity.setReason(snapshot.reason());
        entity.setCreatedAt(snapshot.createdAt());
        entity.setRuleId(snapshot.ruleId());
        entity.setParameterVersion(snapshot.parameterVersion());
        entity.setLegalReference(snapshot.legalReference());
        entity.setSourceUrl(snapshot.sourceUrl());
        return entity;
    }

    private static WithholdingCalculationSnapshot toDomain(WithholdingCalculationSnapshotJpaEntity entity) {
        return new WithholdingCalculationSnapshot(entity.getId(), entity.getCompanyId(), entity.getSourceType(),
                entity.getSourceId(), entity.getThirdPartyId(), entity.getOperationDate(), entity.getWithholdingType(),
                entity.getBaseAmount(), entity.getRate(), entity.getAmount(), entity.getRuleVersion(),
                entity.getDecision(), entity.getReason(), entity.getCreatedAt(), entity.getRuleId(),
                entity.getParameterVersion(), entity.getLegalReference(), entity.getSourceUrl());
    }
}
