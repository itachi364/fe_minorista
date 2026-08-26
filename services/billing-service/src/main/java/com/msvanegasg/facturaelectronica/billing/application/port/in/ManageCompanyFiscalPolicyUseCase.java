package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyResult;

public interface ManageCompanyFiscalPolicyUseCase {

    CompanyFiscalPolicyResult findByCompanyId(UUID companyId);

    CompanyFiscalPolicyResult configure(CompanyFiscalPolicyCommand command);
}
