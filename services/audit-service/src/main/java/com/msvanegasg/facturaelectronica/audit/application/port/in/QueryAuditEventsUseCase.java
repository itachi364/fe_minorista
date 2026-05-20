package com.msvanegasg.facturaelectronica.audit.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;

public interface QueryAuditEventsUseCase {

    List<AuditEventResult> find(AuditEventQuery query);
}
