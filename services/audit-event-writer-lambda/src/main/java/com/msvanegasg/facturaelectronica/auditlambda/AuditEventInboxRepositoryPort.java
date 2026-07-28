package com.msvanegasg.facturaelectronica.auditlambda;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public interface AuditEventInboxRepositoryPort {

    boolean saveIfNew(DomainEventEnvelope envelope, AuditEventWriteRequest request);
}
