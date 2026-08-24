package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

public record CompanyBrandingCommand(
        String displayName,
        String primaryColor,
        String accentColor,
        UUID updatedBy) {
}
