package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingResult;
import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;

public interface ManageCompanyBrandingUseCase {

    CompanyBrandingResult findByCompanyId(UUID companyId);

    CompanyBrandingResult update(UUID companyId, CompanyBrandingCommand command);

    CompanyBrandingResult uploadAsset(UUID companyId, CompanyBrandingAssetCommand command);

    CompanyBrandingAssetResult readAsset(UUID companyId, BrandingAssetPurpose purpose);
}
