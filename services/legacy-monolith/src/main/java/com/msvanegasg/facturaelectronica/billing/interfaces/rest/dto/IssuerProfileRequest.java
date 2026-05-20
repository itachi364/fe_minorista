package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record IssuerProfileRequest(
        @NotBlank String legalName,
        @NotBlank String nit,
        @NotBlank String verificationDigit,
        List<String> taxResponsibilities,
        String municipalityCode,
        String address) {
}
