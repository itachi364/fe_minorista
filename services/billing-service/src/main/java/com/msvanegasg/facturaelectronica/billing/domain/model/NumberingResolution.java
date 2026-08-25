package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public final class NumberingResolution {

    private static final Pattern PREFIX_PATTERN = Pattern.compile("^[A-Za-z0-9]{0,4}$");

    private final UUID id;
    private final UUID companyId;
    private final ElectronicDocumentType documentType;
    private final String resolutionNumber;
    private final String prefix;
    private final long fromNumber;
    private final long toNumber;
    private long currentNumber;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private final FiscalEnvironment environment;
    private final boolean active;

    private NumberingResolution(UUID id, UUID companyId, ElectronicDocumentType documentType, String resolutionNumber,
            String prefix, long fromNumber, long toNumber, long currentNumber, LocalDate validFrom, LocalDate validTo,
            FiscalEnvironment environment, boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.documentType = documentType;
        this.resolutionNumber = resolutionNumber;
        this.prefix = prefix;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.currentNumber = currentNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.environment = environment;
        this.active = active;
    }

    public static NumberingResolution create(UUID id, UUID companyId, ElectronicDocumentType documentType,
            String resolutionNumber, String prefix, long fromNumber, long toNumber, LocalDate validFrom,
            LocalDate validTo, FiscalEnvironment environment) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(documentType, "documentType");
        requireNonBlank(resolutionNumber, "resolutionNumber");
        requireNonNull(validFrom, "validFrom");
        requireNonNull(validTo, "validTo");
        requireNonNull(environment, "environment");
        validateRange(fromNumber, toNumber);
        validateDates(validFrom, validTo);
        return new NumberingResolution(id, companyId, documentType, resolutionNumber.trim(), normalizePrefix(prefix),
                fromNumber, toNumber, fromNumber - 1, validFrom, validTo, environment, true);
    }

    public static NumberingResolution restore(UUID id, UUID companyId, ElectronicDocumentType documentType,
            String resolutionNumber, String prefix, long fromNumber, long toNumber, long currentNumber,
            LocalDate validFrom, LocalDate validTo, FiscalEnvironment environment, boolean active) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(documentType, "documentType");
        requireNonBlank(resolutionNumber, "resolutionNumber");
        requireNonNull(validFrom, "validFrom");
        requireNonNull(validTo, "validTo");
        requireNonNull(environment, "environment");
        validateRange(fromNumber, toNumber);
        validateDates(validFrom, validTo);
        return new NumberingResolution(id, companyId, documentType, resolutionNumber.trim(), normalizePrefix(prefix),
                fromNumber, toNumber, currentNumber, validFrom, validTo, environment, active);
    }

    public boolean isAvailableFor(UUID requestedCompanyId, ElectronicDocumentType requestedDocumentType,
            LocalDate documentDate, FiscalEnvironment requestedEnvironment) {
        return active && companyId.equals(requestedCompanyId) && documentType == requestedDocumentType
                && environment == requestedEnvironment && !documentDate.isBefore(validFrom)
                && !documentDate.isAfter(validTo) && currentNumber < toNumber;
    }

    public FiscalNumberAssignment assignNextNumber(UUID requestedCompanyId, ElectronicDocumentType requestedDocumentType,
            LocalDate documentDate, FiscalEnvironment requestedEnvironment) {
        requireNonNull(requestedCompanyId, "companyId");
        requireNonNull(requestedDocumentType, "documentType");
        requireNonNull(documentDate, "documentDate");
        requireNonNull(requestedEnvironment, "environment");
        if (!isAvailableFor(requestedCompanyId, requestedDocumentType, documentDate, requestedEnvironment)) {
            throw new IllegalStateException("numbering resolution is not available");
        }
        currentNumber += 1;
        return new FiscalNumberAssignment(id, resolutionNumber, prefix, currentNumber);
    }

    public UUID id() { return id; }
    public UUID companyId() { return companyId; }
    public ElectronicDocumentType documentType() { return documentType; }
    public String resolutionNumber() { return resolutionNumber; }
    public String prefix() { return prefix; }
    public long fromNumber() { return fromNumber; }
    public long toNumber() { return toNumber; }
    public long currentNumber() { return currentNumber; }
    public LocalDate validFrom() { return validFrom; }
    public LocalDate validTo() { return validTo; }
    public FiscalEnvironment environment() { return environment; }
    public boolean active() { return active; }

    public NumberingResolution activate() {
        if (active) {
            return this;
        }
        return new NumberingResolution(id, companyId, documentType, resolutionNumber, prefix, fromNumber, toNumber,
                currentNumber, validFrom, validTo, environment, true);
    }

    public NumberingResolution deactivate() {
        if (!active) {
            return this;
        }
        return new NumberingResolution(id, companyId, documentType, resolutionNumber, prefix, fromNumber, toNumber,
                currentNumber, validFrom, validTo, environment, false);
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        String normalized = prefix.trim().toUpperCase();
        if (!PREFIX_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("prefix must be alphanumeric and up to 4 characters");
        }
        return normalized;
    }

    private static void validateRange(long fromNumber, long toNumber) {
        if (fromNumber <= 0) {
            throw new IllegalArgumentException("fromNumber must be positive");
        }
        if (toNumber < fromNumber) {
            throw new IllegalArgumentException("toNumber must be greater than or equal to fromNumber");
        }
    }

    private static void validateDates(LocalDate validFrom, LocalDate validTo) {
        if (validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo must be greater than or equal to validFrom");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
