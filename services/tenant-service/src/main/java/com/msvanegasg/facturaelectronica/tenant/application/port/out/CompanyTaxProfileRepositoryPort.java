package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;

public interface CompanyTaxProfileRepositoryPort {
    Optional<CompanyTaxProfile> findByCompanyId(UUID companyId);

    default Optional<CompanyTaxProfile> findEffective(UUID companyId, LocalDate effectiveOn) {
        return findByCompanyId(companyId);
    }

    CompanyTaxProfile save(CompanyTaxProfile profile);
}
