package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;

public interface ManageCompanyUseCase {

    CompanyResult create(CreateCompanyCommand command);

    CompanyResult findById(UUID companyId);

    CompanyResult activate(UUID companyId);

    CompanyResult suspend(UUID companyId);
}
