package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseValidationResult;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;

public interface ManageCompanyLicenseUseCase {

    CompanyLicenseResult save(UUID companyId, CompanyLicenseCommand command);

    CompanyLicenseResult findByCompanyId(UUID companyId);

    CompanyLicenseResult activate(UUID companyId);

    CompanyLicenseResult suspend(UUID companyId);

    CompanyLicenseValidationResult validate(UUID companyId, LicenseAction action, LicenseModule module);
}
