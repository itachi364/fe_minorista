package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryWithholdingSnapshotsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;

public class QueryWithholdingSnapshotsService implements QueryWithholdingSnapshotsUseCase {
    private final WithholdingCalculationSnapshotRepositoryPort repository;

    public QueryWithholdingSnapshotsService(WithholdingCalculationSnapshotRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<WithholdingCalculationSnapshot> find(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
        return repository.findBySource(Objects.requireNonNull(companyId), Objects.requireNonNull(sourceType),
                Objects.requireNonNull(sourceId));
    }
}
