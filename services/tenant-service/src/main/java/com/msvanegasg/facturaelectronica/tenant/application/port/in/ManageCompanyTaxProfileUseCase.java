package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileResult;

public interface ManageCompanyTaxProfileUseCase {
    CompanyTaxProfileResult findByCompanyId(UUID companyId);

    CompanyTaxProfileResult findByCompanyId(UUID companyId, LocalDate effectiveOn);

    CompanyTaxProfileResult update(UUID companyId, CompanyTaxProfileCommand command);
}
