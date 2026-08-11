package com.msvanegasg.facturaelectronica.audit.application.port.out;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;

public interface AuditEventRepositoryPort {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> find(AuditEventQuery query);

    List<String> resourceTypes(UUID companyId);
}
