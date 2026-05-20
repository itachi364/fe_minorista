package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.application.dto.AuditEventCommand;

public interface AuditEventPort {

    void register(AuditEventCommand command);
}
