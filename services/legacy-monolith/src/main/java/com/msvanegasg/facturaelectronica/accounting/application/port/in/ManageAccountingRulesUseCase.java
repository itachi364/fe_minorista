package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;

public interface ManageAccountingRulesUseCase {

    AccountingRuleResult create(CreateAccountingRuleCommand command);
}
