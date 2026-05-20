package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class IssuerProfile {

    private final UUID id;
    private final UUID companyId;
    private final String legalName;
    private final String nit;
    private final String verificationDigit;
    private final List<String> taxResponsibilities;
    private final String municipalityCode;
    private final String address;
    private final boolean active;

    private IssuerProfile(
            UUID id,
            UUID companyId,
            String legalName,
            String nit,
            String verificationDigit,
            List<String> taxResponsibilities,
            String municipalityCode,
            String address,
            boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.legalName = legalName;
        this.nit = nit;
        this.verificationDigit = verificationDigit;
        this.taxResponsibilities = taxResponsibilities;
        this.municipalityCode = municipalityCode;
        this.address = address;
        this.active = active;
    }

    public static IssuerProfile configure(
            UUID id,
            UUID companyId,
            String legalName,
            String nit,
            String verificationDigit,
            List<String> taxResponsibilities,
            String municipalityCode,
            String address) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonBlank(legalName, "legalName");
        requireNonBlank(nit, "nit");
        requireNonBlank(verificationDigit, "verificationDigit");

        return new IssuerProfile(
                id,
                companyId,
                legalName.trim(),
                nit.trim(),
                verificationDigit.trim(),
                taxResponsibilities == null ? List.of() : List.copyOf(taxResponsibilities),
                blankToNull(municipalityCode),
                blankToNull(address),
                true);
    }

    public static IssuerProfile restore(
            UUID id,
            UUID companyId,
            String legalName,
            String nit,
            String verificationDigit,
            List<String> taxResponsibilities,
            String municipalityCode,
            String address,
            boolean active) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonBlank(legalName, "legalName");
        requireNonBlank(nit, "nit");
        requireNonBlank(verificationDigit, "verificationDigit");

        return new IssuerProfile(
                id,
                companyId,
                legalName.trim(),
                nit.trim(),
                verificationDigit.trim(),
                taxResponsibilities == null ? List.of() : List.copyOf(taxResponsibilities),
                blankToNull(municipalityCode),
                blankToNull(address),
                active);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public String legalName() {
        return legalName;
    }

    public String nit() {
        return nit;
    }

    public String verificationDigit() {
        return verificationDigit;
    }

    public List<String> taxResponsibilities() {
        return taxResponsibilities;
    }

    public String municipalityCode() {
        return municipalityCode;
    }

    public String address() {
        return address;
    }

    public boolean active() {
        return active;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
