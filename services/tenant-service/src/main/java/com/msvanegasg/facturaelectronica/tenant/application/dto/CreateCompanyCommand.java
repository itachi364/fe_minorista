package com.msvanegasg.facturaelectronica.tenant.application.dto;

public record CreateCompanyCommand(
        String legalName,
        String tradeName,
        Integer identificationTypeCode,
        String identificationNumber,
        String verificationDigit,
        String email) {
}
