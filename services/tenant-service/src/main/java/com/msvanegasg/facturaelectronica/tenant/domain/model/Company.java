package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Company(
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

    public static Company create(
            UUID id,
            String legalName,
            String tradeName,
            UUID identificationTypeId,
            String identificationNumber,
            String verificationDigit,
            String email,
            Instant now) {
        validateRequired(id, "id");
        validateText(legalName, "legalName");
        validateRequired(identificationTypeId, "identificationTypeId");
        validateText(identificationNumber, "identificationNumber");
        validateText(email, "email");
        validateRequired(now, "now");
        return new Company(
                id,
                legalName.trim(),
                normalizeNullable(tradeName),
                identificationTypeId,
                identificationNumber.trim(),
                normalizeNullable(verificationDigit),
                email.trim(),
                CompanyStatus.ACTIVE,
                now,
                now);
    }

    public Company activate(Instant now) {
        validateRequired(now, "now");
        return new Company(id, legalName, tradeName, identificationTypeId, identificationNumber, verificationDigit,
                email, CompanyStatus.ACTIVE, createdAt, now);
    }

    public Company suspend(Instant now) {
        validateRequired(now, "now");
        return new Company(id, legalName, tradeName, identificationTypeId, identificationNumber, verificationDigit,
                email, CompanyStatus.SUSPENDED, createdAt, now);
    }

    private static void validateText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static void validateRequired(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
