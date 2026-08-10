package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.util.UUID;

public record CatalogAuditEventCommand(UUID companyId, UUID userId, String eventType, String resourceType,
        String resourceId, String action, String result, String detail) {
}
