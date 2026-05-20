package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;

public interface ManageChartOfAccountsUseCase {

    AccountResult create(CreateAccountCommand command);

    AccountResult findByCode(UUID companyId, String code);
}
