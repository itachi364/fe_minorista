package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayablePayment;

public interface AccountsPayablePaymentRepositoryPort {

    AccountsPayablePayment save(AccountsPayablePayment payment);
}
