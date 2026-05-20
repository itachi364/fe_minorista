package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

public record CreateCompanyCommand(
        String legalName,
        String tradeName,
        UUID identificationTypeId,
        String identificationNumber,
        String verificationDigit,
        String email) {
}
