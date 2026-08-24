package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyBranding;

public interface CompanyBrandingRepositoryPort {

    CompanyBranding save(CompanyBranding branding);

    Optional<CompanyBranding> findByCompanyId(UUID companyId);
}
