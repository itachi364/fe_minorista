package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;

public interface CompanyTaxProfilePort {
    Optional<CompanyTaxProfile> findByCompanyId(UUID companyId);
}
