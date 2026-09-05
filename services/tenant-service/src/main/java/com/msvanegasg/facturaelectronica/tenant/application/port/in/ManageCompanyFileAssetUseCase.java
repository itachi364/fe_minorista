package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileContentResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileDownloadLinkResult;

public interface ManageCompanyFileAssetUseCase {

    CompanyFileAssetResult upload(UUID companyId, CompanyFileAssetCommand command);

    CompanyFileContentResult read(UUID companyId, UUID assetId);

    CompanyFileContentResult readSigned(UUID companyId, UUID assetId, long expiresAtEpochSecond, String signature);

    CompanyFileDownloadLinkResult createDownloadLink(UUID companyId, UUID assetId);
}
