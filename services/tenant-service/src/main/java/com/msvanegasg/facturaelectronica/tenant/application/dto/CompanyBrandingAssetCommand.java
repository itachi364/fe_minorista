package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;

public record CompanyBrandingAssetCommand(
        BrandingAssetPurpose purpose,
        String originalFilename,
        String contentType,
        byte[] content,
        UUID updatedBy) {
}
