package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.util.UUID;

public class CompanyLicenseNotFoundException extends RuntimeException {

    public CompanyLicenseNotFoundException(UUID companyId) {
        super("No existe licencia configurada para la empresa " + companyId + ".");
    }
}
