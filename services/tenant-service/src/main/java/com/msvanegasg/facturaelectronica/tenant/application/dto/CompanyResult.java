package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;

public record CompanyResult(
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

    public static CompanyResult from(Company company) {
        return new CompanyResult(
                company.id(),
                company.legalName(),
                company.tradeName(),
                company.identificationTypeId(),
                company.identificationNumber(),
                company.verificationDigit(),
                company.email(),
                company.status(),
                company.createdAt(),
                company.updatedAt());
    }
}
