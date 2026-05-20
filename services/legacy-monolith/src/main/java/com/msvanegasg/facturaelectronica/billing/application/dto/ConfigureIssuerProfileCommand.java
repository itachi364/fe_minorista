package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

public record ConfigureIssuerProfileCommand(
        UUID companyId,
        String legalName,
        String nit,
        String verificationDigit,
        List<String> taxResponsibilities,
        String municipalityCode,
        String address) {
}
