package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MunicipalFiscalPackageRequest(
        @NotBlank String municipalityCode,
        @NotBlank String packageCode,
        @NotBlank String version,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        @NotBlank String legalReference,
        @NotBlank String officialSourceUrl,
        @NotEmpty List<@Valid MunicipalReteicaRuleRequest> rules) {
}
