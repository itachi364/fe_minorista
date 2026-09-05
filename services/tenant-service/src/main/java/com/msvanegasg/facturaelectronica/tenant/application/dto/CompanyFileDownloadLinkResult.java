package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyFileDownloadLinkResult(
        UUID assetId,
        UUID companyId,
        String url,
        Instant expiresAt,
        long ttlSeconds) {
}
