package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsPayableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterPayablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;

public interface ManageAccountsPayableUseCase {

    AccountsPayableResult create(CreateAccountsPayableCommand command);

    List<AccountsPayableResult> find(UUID companyId, AccountsPayableStatus status, UUID supplierId, LocalDate from,
            LocalDate to);

    AccountsPayablePaymentResult registerPayment(RegisterPayablePaymentCommand command);
}
