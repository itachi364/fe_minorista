package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;

public interface GenerateAccountingEntryUseCase {

    AccountingEntryResult generate(GenerateAccountingEntryCommand command);
}
