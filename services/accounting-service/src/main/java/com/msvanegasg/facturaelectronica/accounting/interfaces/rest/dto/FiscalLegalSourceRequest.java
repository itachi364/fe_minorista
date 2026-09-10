package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record FiscalLegalSourceRequest(
        @NotBlank String code,
        @NotBlank String title,
        @NotBlank String authority,
        @NotBlank String officialUrl,
        LocalDate issuedOn,
        LocalDate reviewDueOn) {
}
