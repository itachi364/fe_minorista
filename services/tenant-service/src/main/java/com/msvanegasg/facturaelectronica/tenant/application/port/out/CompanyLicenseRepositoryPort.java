package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;

public interface CompanyLicenseRepositoryPort {

    CompanyLicense save(CompanyLicense license);

    Optional<CompanyLicense> findByCompanyId(UUID companyId);
}
