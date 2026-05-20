package com.msvanegasg.facturaelectronica.audit.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.application.dto.RegisterAuditEventCommand;
import com.msvanegasg.facturaelectronica.audit.interfaces.rest.dto.AuditEventRequest;
import com.msvanegasg.facturaelectronica.audit.interfaces.rest.dto.AuditEventResponse;

public final class AuditRestMapper {

    private AuditRestMapper() {
    }

    public static RegisterAuditEventCommand toCommand(UUID companyId, AuditEventRequest request) {
        return new RegisterAuditEventCommand(companyId, request.userId(), request.eventType(), request.resourceType(),
                request.resourceId(), request.action(), request.result(), request.detail());
    }

    public static AuditEventResponse toResponse(AuditEventResult result) {
        return new AuditEventResponse(result.id(), result.companyId(), result.userId(), result.eventType(),
                result.resourceType(), result.resourceId(), result.action(), result.result(), result.detail(),
                result.occurredAt());
    }
}
