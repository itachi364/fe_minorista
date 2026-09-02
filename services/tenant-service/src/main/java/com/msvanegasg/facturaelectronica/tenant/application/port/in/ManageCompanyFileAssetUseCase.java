package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileContentResult;

public interface ManageCompanyFileAssetUseCase {

    CompanyFileAssetResult upload(UUID companyId, CompanyFileAssetCommand command);

    CompanyFileContentResult read(UUID companyId, UUID assetId);
}
