package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;

public interface AccountingRuleRepositoryPort {

    Optional<AccountingRule> findActiveByCompanyIdAndEventType(UUID companyId, AccountingEventType eventType);

    List<AccountingRule> findByCompanyId(UUID companyId, AccountingEventType eventType, Boolean active);

    AccountingRule save(AccountingRule rule);
}