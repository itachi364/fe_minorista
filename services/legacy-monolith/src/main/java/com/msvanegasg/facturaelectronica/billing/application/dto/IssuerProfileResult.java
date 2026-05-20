package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

public record IssuerProfileResult(
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
