package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

public record IssuerProfileResponse(
        UUID id,
        UUID companyId,
        String legalName,
        String nit,
        String verificationDigit,
        List<String> taxResponsibilities,
        String municipalityCode,
        String address,
        boolean active) {
}
