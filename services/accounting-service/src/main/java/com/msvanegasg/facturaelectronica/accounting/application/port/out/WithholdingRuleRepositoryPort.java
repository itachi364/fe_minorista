package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;

public interface WithholdingRuleRepositoryPort {

    List<WithholdingRule> findActiveRules(UUID companyId, FiscalOperationType operationType, LocalDate operationDate);
    default List<WithholdingRule> findAll() { return List.of(); }
    default java.util.Optional<WithholdingRule> findById(UUID id) { return java.util.Optional.empty(); }
    default WithholdingRule save(WithholdingRule rule) { throw new UnsupportedOperationException("save"); }
}
