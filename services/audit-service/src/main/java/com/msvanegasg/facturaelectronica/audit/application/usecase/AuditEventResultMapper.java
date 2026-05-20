package com.msvanegasg.facturaelectronica.audit.application.usecase;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;

final class AuditEventResultMapper {

    private AuditEventResultMapper() {
    }

    static AuditEventResult toResult(AuditEvent event) {
        return new AuditEventResult(event.id(), event.companyId(), event.userId(), event.eventType(),
                event.resourceType(), event.resourceId(), event.action(), event.result(), event.detail(),
                event.occurredAt());
    }
}
