package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;

public interface ManageAccountingRulesUseCase {

    AccountingRuleResult create(CreateAccountingRuleCommand command);

    AccountingRuleResult replaceActive(CreateAccountingRuleCommand command);

    List<AccountingRuleResult> replaceActiveAll(List<CreateAccountingRuleCommand> commands);

    AccountingRuleResult deactivateActive(UUID companyId, AccountingEventType eventType);

    List<AccountingRuleResult> find(UUID companyId, AccountingEventType eventType, Boolean active);
}
