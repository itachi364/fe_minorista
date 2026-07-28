package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivablePayment;

public interface AccountsReceivablePaymentRepositoryPort {

    AccountsReceivablePayment save(AccountsReceivablePayment payment);
}