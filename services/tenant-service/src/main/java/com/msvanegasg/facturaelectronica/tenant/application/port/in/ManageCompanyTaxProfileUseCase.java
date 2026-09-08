package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileResult;

public interface ManageCompanyTaxProfileUseCase {
    CompanyTaxProfileResult findByCompanyId(UUID companyId);

    CompanyTaxProfileResult update(UUID companyId, CompanyTaxProfileCommand command);
}
