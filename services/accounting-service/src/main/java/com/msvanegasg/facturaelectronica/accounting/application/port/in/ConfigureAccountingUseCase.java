package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingConfigurationCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;

public interface ConfigureAccountingUseCase {

    AccountingSetupResult configure(AccountingConfigurationCommand command);
}
