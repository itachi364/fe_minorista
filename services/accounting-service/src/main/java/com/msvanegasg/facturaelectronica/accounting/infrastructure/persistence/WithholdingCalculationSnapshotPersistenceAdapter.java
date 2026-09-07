package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
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
        return entity;
    }
}
