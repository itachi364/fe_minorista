package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CompanyLicense(
        UUID id,
        UUID companyId,
        String planCode,
        CompanyLicenseStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
        Instant createdAt,
        Instant updatedAt) {

    public static CompanyLicense create(
            UUID id,
            UUID companyId,
            String planCode,
            LocalDate validFrom,
            LocalDate validTo,
            Integer maxUsers,
            Integer maxMonthlyDocuments,
            Instant now) {
        validateRequired(id, "id");
        validateRequired(companyId, "companyId");
        validateText(planCode, "planCode");
        validateDateRange(validFrom, validTo);
        validateLimit(maxUsers, "maxUsers");
        validateLimit(maxMonthlyDocuments, "maxMonthlyDocuments");
        validateRequired(now, "now");
        return new CompanyLicense(
                id,
                companyId,
                planCode.trim(),
                CompanyLicenseStatus.ACTIVE,
                validFrom,
                validTo,
                maxUsers,
                maxMonthlyDocuments,
                now,
                now);
    }

    public CompanyLicense update(
            String planCode,
            LocalDate validFrom,
            LocalDate validTo,
            Integer maxUsers,
            Integer maxMonthlyDocuments,
            Instant now) {
        validateText(planCode, "planCode");
        validateDateRange(validFrom, validTo);
        validateLimit(maxUsers, "maxUsers");
        validateLimit(maxMonthlyDocuments, "maxMonthlyDocuments");
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, planCode.trim(), status, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, createdAt, now);
    }

    public CompanyLicense activate(Instant now) {
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, planCode, CompanyLicenseStatus.ACTIVE, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, createdAt, now);
    }

    public CompanyLicense suspend(Instant now) {
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, planCode, CompanyLicenseStatus.SUSPENDED, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, createdAt, now);
    }

    public CompanyLicenseStatus effectiveStatus(LocalDate today) {
        validateRequired(today, "today");
        if (status == CompanyLicenseStatus.ACTIVE && validTo.isBefore(today)) {
            return CompanyLicenseStatus.EXPIRED;
        }
        return status;
    }

    public boolean allows(LicenseAction action, LocalDate today) {
        validateRequired(action, "action");
        return effectiveStatus(today) == CompanyLicenseStatus.ACTIVE;
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

    private static void validateDateRange(LocalDate validFrom, LocalDate validTo) {
        validateRequired(validFrom, "validFrom");
        validateRequired(validTo, "validTo");
        if (validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo must be on or after validFrom");
        }
    }

    private static void validateLimit(Integer value, String field) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
    }
}
