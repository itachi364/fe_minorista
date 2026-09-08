package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;

public interface QueryWithholdingSnapshotsUseCase {
    List<WithholdingCalculationSnapshot> find(UUID companyId, AccountingSourceType sourceType, UUID sourceId);
}
