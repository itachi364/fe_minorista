package com.msvanegasg.facturaelectronica.audit.application.port.in;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.application.dto.RegisterAuditEventCommand;

public interface RegisterAuditEventUseCase {

    AuditEventResult register(RegisterAuditEventCommand command);
}
