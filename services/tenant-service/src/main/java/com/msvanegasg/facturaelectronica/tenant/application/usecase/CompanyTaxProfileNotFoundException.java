package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.util.UUID;

public class CompanyTaxProfileNotFoundException extends RuntimeException {
    public CompanyTaxProfileNotFoundException(UUID companyId) {
        super("Company tax profile was not found: " + companyId);
    }
}
