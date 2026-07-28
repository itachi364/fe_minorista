package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsReceivableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterReceivablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;

public interface ManageAccountsReceivableUseCase {

    AccountsReceivableResult create(CreateAccountsReceivableCommand command);

    List<AccountsReceivableResult> find(UUID companyId, AccountsReceivableStatus status, UUID customerId,
            LocalDate from, LocalDate to);

    AccountsReceivablePaymentResult registerPayment(RegisterReceivablePaymentCommand command);
}