package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogDefinitionResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.DepartmentResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.MunicipalityResult;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogDefinitionResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogItemRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogItemResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DepartmentResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.MunicipalityResponse;

public final class VersionedCatalogRestMapper {

    private VersionedCatalogRestMapper() {
    }

    public static CatalogItemResponse toResponse(CatalogItemResult result) {
        return new CatalogItemResponse(result.catalogCode(), result.code(), result.label(), result.description(),
                result.active(), result.enabledForCompany(), result.regulatory(), result.source(),
                result.sourceVersion(), result.validFrom(), result.validTo(), result.sortOrder());
    }

    public static CatalogDefinitionResponse toResponse(CatalogDefinitionResult result) {
        return new CatalogDefinitionResponse(result.code(), result.label(), result.description(), result.regulatory(),
                result.companyConfigurable(), result.globalEditableByRoot(), result.active(), result.sortOrder());
    }

    public static CatalogItemCommand toCommand(CatalogItemRequest request) {
        return new CatalogItemCommand(request.code(), request.label(), request.description(), request.regulatory(),
                request.source(), request.sourceVersion(), request.validFrom(), request.validTo(),
                request.sortOrder() == null ? 0 : request.sortOrder());
    }

    public static DepartmentResponse toResponse(DepartmentResult result) {
        return new DepartmentResponse(result.code(), result.name(), result.active(), result.source(),
                result.sourceVersion());
    }

    public static MunicipalityResponse toResponse(MunicipalityResult result) {
        return new MunicipalityResponse(result.code(), result.departmentCode(), result.name(), result.active(),
                result.source(), result.sourceVersion());
    }
}
