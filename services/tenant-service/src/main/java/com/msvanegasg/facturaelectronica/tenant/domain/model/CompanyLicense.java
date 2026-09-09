package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
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
        Set<LicenseModule> enabledModules,
        Set<LicenseFeature> enabledFeatures,
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
            Set<LicenseModule> enabledModules,
            Set<LicenseFeature> enabledFeatures,
            Instant now) {
        validateRequired(id, "id");
        validateRequired(companyId, "companyId");
        validateText(planCode, "planCode");
        validateDateRange(validFrom, validTo);
        validateLimit(maxUsers, "maxUsers");
        validateLimit(maxMonthlyDocuments, "maxMonthlyDocuments");
        LicensePlanPolicy.Selection selection = LicensePlanPolicy.normalize(planCode, enabledModules, enabledFeatures);
        validateRequired(now, "now");
        return new CompanyLicense(
                id,
                companyId,
                selection.planCode(),
                CompanyLicenseStatus.ACTIVE,
                validFrom,
                validTo,
                maxUsers,
                maxMonthlyDocuments,
                selection.modules(),
                selection.features(),
                now,
                now);
    }

    public CompanyLicense update(
            String planCode,
            LocalDate validFrom,
            LocalDate validTo,
            Integer maxUsers,
            Integer maxMonthlyDocuments,
            Set<LicenseModule> enabledModules,
            Set<LicenseFeature> enabledFeatures,
            Instant now) {
        validateText(planCode, "planCode");
        validateDateRange(validFrom, validTo);
        validateLimit(maxUsers, "maxUsers");
        validateLimit(maxMonthlyDocuments, "maxMonthlyDocuments");
        LicensePlanPolicy.Selection selection = LicensePlanPolicy.normalize(planCode, enabledModules, enabledFeatures);
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, selection.planCode(), status, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, selection.modules(), selection.features(), createdAt, now);
    }

    public CompanyLicense activate(Instant now) {
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, planCode, CompanyLicenseStatus.ACTIVE, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, enabledModules, enabledFeatures, createdAt, now);
    }

    public CompanyLicense suspend(Instant now) {
        validateRequired(now, "now");
        return new CompanyLicense(id, companyId, planCode, CompanyLicenseStatus.SUSPENDED, validFrom, validTo, maxUsers,
                maxMonthlyDocuments, enabledModules, enabledFeatures, createdAt, now);
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

    public boolean allows(LicenseAction action, LicenseModule module, LocalDate today) {
        return allows(action, module, null, today);
    }

    public boolean allows(LicenseAction action, LicenseModule module, LicenseFeature feature, LocalDate today) {
        validateRequired(action, "action");
        return allows(action, today)
                && (module == null || enabledModules().contains(module))
                && (feature == null || enabledFeatures().contains(feature));
    }

    @Override
    public Set<LicenseModule> enabledModules() {
        return enabledModules == null ? Set.of() : Collections.unmodifiableSet(enabledModules);
    }

    @Override
    public Set<LicenseFeature> enabledFeatures() {
        return enabledFeatures == null ? Set.of() : Collections.unmodifiableSet(enabledFeatures);
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
