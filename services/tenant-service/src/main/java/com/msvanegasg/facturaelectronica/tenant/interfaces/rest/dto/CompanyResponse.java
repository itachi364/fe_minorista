package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;

public record CompanyResponse(
        UUID id,
        String legalName,
        String tradeName,
        UUID identificationTypeId,
        String identificationNumber,
        String verificationDigit,
        String email,
        CompanyStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
