package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;

public interface ManageChartOfAccountsUseCase {

    AccountResult create(CreateAccountCommand command);

    List<AccountResult> createAll(List<CreateAccountCommand> commands);

    AccountResult update(UUID companyId, UUID accountId, CreateAccountCommand command);

    AccountResult deactivate(UUID companyId, UUID accountId);

    AccountResult findByCode(UUID companyId, String code);

    List<AccountResult> find(UUID companyId, Boolean active);
}
