package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;

public record CompanyFileAssetCommand(
        CompanyFileCategory category,
        String originalFilename,
        String contentType,
        byte[] content,
        UUID uploadedBy) {
}
