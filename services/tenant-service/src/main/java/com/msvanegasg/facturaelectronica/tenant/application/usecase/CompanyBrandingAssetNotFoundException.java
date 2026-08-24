package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;

public class CompanyBrandingAssetNotFoundException extends RuntimeException {

    public CompanyBrandingAssetNotFoundException(UUID companyId, BrandingAssetPurpose purpose) {
        super("Branding asset not found for company %s and purpose %s".formatted(companyId, purpose));
    }
}
