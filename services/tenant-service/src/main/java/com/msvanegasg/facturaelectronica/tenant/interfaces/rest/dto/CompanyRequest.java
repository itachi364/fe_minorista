package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 180) String legalName,
        @Size(max = 180) String tradeName,
        @NotNull UUID identificationTypeId,
        @NotBlank @Size(max = 30) String identificationNumber,
        @Size(max = 2) String verificationDigit,
        @NotBlank @Email @Size(max = 180) String email) {
}
