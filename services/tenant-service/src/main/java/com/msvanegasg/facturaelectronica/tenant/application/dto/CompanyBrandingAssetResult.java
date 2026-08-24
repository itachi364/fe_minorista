package com.msvanegasg.facturaelectronica.tenant.application.dto;

public record CompanyBrandingAssetResult(
        byte[] content,
        String contentType,
        String contentHash) {
}
