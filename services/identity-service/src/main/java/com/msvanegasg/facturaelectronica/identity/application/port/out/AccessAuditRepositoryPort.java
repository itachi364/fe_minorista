package com.msvanegasg.facturaelectronica.identity.application.port.out;

import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditEvent;

public interface AccessAuditRepositoryPort {

    AccessAuditEvent save(AccessAuditEvent event);
}
