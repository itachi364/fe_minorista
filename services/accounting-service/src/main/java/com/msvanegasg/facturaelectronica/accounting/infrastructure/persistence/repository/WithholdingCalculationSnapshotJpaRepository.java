package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.WithholdingCalculationSnapshotJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface WithholdingCalculationSnapshotJpaRepository
        extends JpaRepository<WithholdingCalculationSnapshotJpaEntity, UUID> {
    List<WithholdingCalculationSnapshotJpaEntity> findByCompanyIdAndSourceTypeAndSourceIdOrderByWithholdingType(
            UUID companyId, AccountingSourceType sourceType, UUID sourceId);
}
