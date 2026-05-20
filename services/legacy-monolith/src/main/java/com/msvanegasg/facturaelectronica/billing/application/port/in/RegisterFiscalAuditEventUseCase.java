package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAuditEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterFiscalAuditEventCommand;

public interface RegisterFiscalAuditEventUseCase {

    FiscalAuditEventResult register(RegisterFiscalAuditEventCommand command);
}
