package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingResult;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyBrandingRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyBrandingResponse;

public final class CompanyBrandingRestMapper {

    private CompanyBrandingRestMapper() {
    }

    public static CompanyBrandingCommand toCommand(CompanyBrandingRequest request, UUID updatedBy) {
        return new CompanyBrandingCommand(request.displayName(), request.primaryColor(), request.accentColor(), updatedBy);
    }

    public static CompanyBrandingResponse toResponse(CompanyBrandingResult result) {
        return new CompanyBrandingResponse(result.companyId(), result.displayName(), result.primaryColor(),
                result.accentColor(), result.mainLogoUrl(), result.headerLogoUrl(), result.loginLogoUrl(),
                result.faviconUrl(), result.updatedAt());
    }
}
