package com.msvanegasg.facturaelectronica.tenant.application.dto;

public record CompanyFileContentResult(
        String originalFilename,
        String contentType,
        String contentHash,
        byte[] content) {
}
