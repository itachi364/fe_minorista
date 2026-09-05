package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileDownloadLinkResult;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyFileAssetResponse;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyFileDownloadLinkResponse;

final class CompanyFileAssetRestMapper {

    private CompanyFileAssetRestMapper() {
    }

    static CompanyFileAssetResponse toResponse(CompanyFileAssetResult result) {
        return new CompanyFileAssetResponse(result.id(), result.companyId(), result.category(),
                result.originalFilename(), result.contentType(), result.fileSize(), result.contentHash(),
                result.url(), result.uploadedBy(), result.uploadedAt());
    }

    static CompanyFileDownloadLinkResponse toResponse(CompanyFileDownloadLinkResult result) {
        return new CompanyFileDownloadLinkResponse(result.assetId(), result.companyId(), result.url(),
                result.expiresAt(), result.ttlSeconds());
    }
}
