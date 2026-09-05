package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyFileDownloadLinkResponse(
        UUID assetId,
        UUID companyId,
        String url,
        Instant expiresAt,
        long ttlSeconds) {
}
