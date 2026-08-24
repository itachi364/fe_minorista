package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyBrandingResponse(
        UUID companyId,
        String displayName,
        String primaryColor,
        String accentColor,
        String mainLogoUrl,
        String headerLogoUrl,
        String loginLogoUrl,
        String faviconUrl,
        Instant updatedAt) {
}
