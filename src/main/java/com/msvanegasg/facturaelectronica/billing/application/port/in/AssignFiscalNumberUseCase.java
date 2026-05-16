package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;

public interface AssignFiscalNumberUseCase {

    FiscalNumberResult assign(AssignFiscalNumberCommand command);
}
