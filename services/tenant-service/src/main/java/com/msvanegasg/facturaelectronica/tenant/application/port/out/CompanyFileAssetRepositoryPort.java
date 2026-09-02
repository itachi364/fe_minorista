package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileAsset;

public interface CompanyFileAssetRepositoryPort {

    CompanyFileAsset save(CompanyFileAsset asset);

    Optional<CompanyFileAsset> findByCompanyIdAndId(UUID companyId, UUID id);
}
