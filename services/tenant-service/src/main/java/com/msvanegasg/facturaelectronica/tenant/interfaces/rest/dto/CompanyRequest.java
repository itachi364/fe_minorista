package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 180) String legalName,
        @Size(max = 180) String tradeName,
        @NotNull @Min(1) @Max(99) Integer identificationTypeCode,
        @NotBlank @Size(max = 30) String identificationNumber,
        @Size(max = 2) String verificationDigit,
        @NotBlank @Email @Size(max = 180) String email) {
}
