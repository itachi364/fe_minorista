package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyFileAssetResponse;

final class CompanyFileAssetRestMapper {

    private CompanyFileAssetRestMapper() {
    }

    static CompanyFileAssetResponse toResponse(CompanyFileAssetResult result) {
        return new CompanyFileAssetResponse(result.id(), result.companyId(), result.category(),
                result.originalFilename(), result.contentType(), result.fileSize(), result.contentHash(),
                result.url(), result.uploadedBy(), result.uploadedAt());
    }
}
