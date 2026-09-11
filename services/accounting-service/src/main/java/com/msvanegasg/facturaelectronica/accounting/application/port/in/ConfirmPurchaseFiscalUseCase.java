package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import com.msvanegasg.facturaelectronica.accounting.application.dto.ConfirmPurchaseFiscalCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.PurchaseFiscalConfirmationResult;

public interface ConfirmPurchaseFiscalUseCase {

    PurchaseFiscalConfirmationResult confirm(ConfirmPurchaseFiscalCommand command);
}
