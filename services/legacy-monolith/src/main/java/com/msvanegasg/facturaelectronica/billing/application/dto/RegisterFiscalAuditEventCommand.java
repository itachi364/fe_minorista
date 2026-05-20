package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

public record RegisterFiscalAuditEventCommand(
        UUID companyId,
        UUID resourceId,
        String resourceType,
        String action,
        String result,
        UUID userId,
        String detail) {
}
