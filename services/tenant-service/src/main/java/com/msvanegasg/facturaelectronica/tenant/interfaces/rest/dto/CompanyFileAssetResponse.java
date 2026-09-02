package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;

public record CompanyFileAssetResponse(
        UUID id,
        UUID companyId,
        CompanyFileCategory category,
        String originalFilename,
        String contentType,
        long fileSize,
        String contentHash,
        String url,
        UUID uploadedBy,
        Instant uploadedAt) {
}
