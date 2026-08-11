package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.Set;

import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompanyLicenseRequest(
        @NotBlank String planCode,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTo,
        @Positive Integer maxUsers,
        @Positive Integer maxMonthlyDocuments,
        Set<LicenseModule> enabledModules) {
}
