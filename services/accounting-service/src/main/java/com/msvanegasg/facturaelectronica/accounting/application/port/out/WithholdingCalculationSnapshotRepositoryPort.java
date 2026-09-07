package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;

public interface WithholdingCalculationSnapshotRepositoryPort {

    void saveAll(List<WithholdingCalculationSnapshot> snapshots);
}
