package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface WithholdingCalculationSnapshotRepositoryPort {

    void saveAll(List<WithholdingCalculationSnapshot> snapshots);

    default List<WithholdingCalculationSnapshot> findBySource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId) {
        return List.of();
    }
}
