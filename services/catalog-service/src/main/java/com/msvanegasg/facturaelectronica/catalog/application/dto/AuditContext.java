package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.util.UUID;

public record AuditContext(UUID companyId, UUID userId, String correlationId) {

    public static AuditContext empty() {
        return new AuditContext(null, null, null);
    }
}
