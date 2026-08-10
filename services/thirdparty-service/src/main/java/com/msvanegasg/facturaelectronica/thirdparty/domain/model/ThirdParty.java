package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ThirdParty {

    private static final int MAX_DOCUMENT_NUMBER_LENGTH = 30;
    private static final int MAX_NAME_LENGTH = 220;
    private static final int MAX_TRADE_NAME_LENGTH = 220;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_PHONE_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 250;
    private static final int MAX_MUNICIPALITY_CODE_LENGTH = 20;
    private static final String SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY = "R-99-PN";

    private final UUID id;
    private final UUID companyId;
    private final PersonType personType;
    private final Integer identificationTypeCode;
    private final String identificationNumber;
    private final Integer verificationDigit;
    private final String fullName;
    private final String businessName;
    private final String tradeName;
    private final String email;
    private final String phone;
    private final String address;
    private final String municipalityCode;
    private final Set<String> taxResponsibilities;
    private final TaxRegime taxRegime;
    private final Set<ThirdPartyRole> roles;
    private final boolean active;

    private ThirdParty(UUID id, UUID companyId, PersonType personType, Integer identificationTypeCode,
            String identificationNumber, Integer verificationDigit, String fullName, String businessName,
            String tradeName, String email, String phone, String address, String municipalityCode,
            Set<String> taxResponsibilities, TaxRegime taxRegime, Set<ThirdPartyRole> roles, boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.personType = personType;
        this.identificationTypeCode = identificationTypeCode;
        this.identificationNumber = identificationNumber;
        this.verificationDigit = verificationDigit;
        this.fullName = fullName;
        this.businessName = businessName;
        this.tradeName = tradeName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.municipalityCode = municipalityCode;
        this.taxResponsibilities = taxResponsibilities;
        this.taxRegime = taxRegime;
        this.roles = roles;
        this.active = active;
    }

    public static ThirdParty create(UUID companyId, PersonType personType, Integer identificationTypeCode,
            String identificationNumber, String fullName, String businessName, String tradeName, String email,
            String phone, String address, String municipalityCode, Set<String> taxResponsibilities,
            TaxRegime taxRegime, Set<ThirdPartyRole> roles) {
        return restore(null, companyId, personType, identificationTypeCode, identificationNumber, null, fullName,
                businessName, tradeName, email, phone, address, municipalityCode, taxResponsibilities, taxRegime,
                roles, true);
    }

    public static ThirdParty restore(UUID id, UUID companyId, PersonType personType, Integer identificationTypeCode,
            String identificationNumber, Integer verificationDigit, String fullName, String businessName,
            String tradeName, String email, String phone, String address, String municipalityCode,
            Set<String> taxResponsibilities, TaxRegime taxRegime, Set<ThirdPartyRole> roles, boolean active) {
        UUID requiredCompanyId = Objects.requireNonNull(companyId, "companyId is required");
        PersonType requiredPersonType = Objects.requireNonNull(personType, "personType is required");
        DianIdentificationTypeCode.validate(identificationTypeCode);
        String documentNumber = normalizeRequired(identificationNumber, MAX_DOCUMENT_NUMBER_LENGTH,
                "identificationNumber");
        Integer calculatedDigit = isNit(identificationTypeCode) ? NitVerificationDigit.calculate(documentNumber) : null;
        validateProvidedDigit(identificationTypeCode, calculatedDigit, verificationDigit);
        String normalizedFullName = normalizeOptional(fullName, MAX_NAME_LENGTH, "fullName");
        String normalizedBusinessName = normalizeOptional(businessName, MAX_NAME_LENGTH, "businessName");
        String normalizedTradeName = normalizeOptional(tradeName, MAX_TRADE_NAME_LENGTH, "tradeName");
        validateName(requiredPersonType, normalizedFullName, normalizedBusinessName);
        Set<ThirdPartyRole> requiredRoles = normalizeRoles(roles);
        boolean simpleNaturalCustomer = isSimpleNaturalCustomer(requiredPersonType, requiredRoles);
        if (simpleNaturalCustomer) {
            validateSimpleNaturalCustomer(identificationTypeCode, normalizedBusinessName, normalizedTradeName);
        }
        Set<String> normalizedTaxResponsibilities = simpleNaturalCustomer
                ? normalizeSimpleNaturalCustomerResponsibilities(taxResponsibilities)
                : TaxResponsibilityCatalog.normalize(taxResponsibilities);
        TaxRegime normalizedTaxRegime = simpleNaturalCustomer ? normalizeSimpleNaturalCustomerRegime(taxRegime)
                : taxRegime;
        return new ThirdParty(id, requiredCompanyId, requiredPersonType, identificationTypeCode, documentNumber,
                calculatedDigit, normalizedFullName, normalizedBusinessName, normalizedTradeName,
                normalizeOptional(email, MAX_EMAIL_LENGTH, "email"), normalizeOptional(phone, MAX_PHONE_LENGTH,
                        "phone"),
                normalizeOptional(address, MAX_ADDRESS_LENGTH, "address"), normalizeOptional(municipalityCode,
                        MAX_MUNICIPALITY_CODE_LENGTH, "municipalityCode"),
                normalizedTaxResponsibilities, normalizedTaxRegime, requiredRoles, active);
    }

    public ThirdParty update(PersonType personType, String fullName, String businessName, String tradeName,
            String email, String phone, String address, String municipalityCode, Set<String> taxResponsibilities,
            TaxRegime taxRegime, Set<ThirdPartyRole> roles) {
        return restore(id, companyId, personType, identificationTypeCode, identificationNumber, verificationDigit,
                fullName, businessName, tradeName, email, phone, address, municipalityCode, taxResponsibilities,
                taxRegime, roles, active);
    }

    public ThirdParty activate() {
        return restore(id, companyId, personType, identificationTypeCode, identificationNumber, verificationDigit,
                fullName, businessName, tradeName, email, phone, address, municipalityCode, taxResponsibilities,
                taxRegime, roles, true);
    }

    public ThirdParty deactivate() {
        return restore(id, companyId, personType, identificationTypeCode, identificationNumber, verificationDigit,
                fullName, businessName, tradeName, email, phone, address, municipalityCode, taxResponsibilities,
                taxRegime, roles, false);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public PersonType personType() {
        return personType;
    }

    public Integer identificationTypeCode() {
        return identificationTypeCode;
    }

    public String identificationNumber() {
        return identificationNumber;
    }

    public Integer verificationDigit() {
        return verificationDigit;
    }

    public String fullName() {
        return fullName;
    }

    public String businessName() {
        return businessName;
    }

    public String tradeName() {
        return tradeName;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public String address() {
        return address;
    }

    public String municipalityCode() {
        return municipalityCode;
    }

    public Set<String> taxResponsibilities() {
        return taxResponsibilities;
    }

    public TaxRegime taxRegime() {
        return taxRegime;
    }

    public Set<ThirdPartyRole> roles() {
        return roles;
    }

    public boolean active() {
        return active;
    }

    public boolean hasRole(ThirdPartyRole role) {
        return roles.contains(role);
    }

    private static boolean isNit(Integer documentType) {
        return Integer.valueOf(31).equals(documentType);
    }

    private static void validateProvidedDigit(Integer documentType, Integer calculatedDigit, Integer providedDigit) {
        if (!isNit(documentType) && providedDigit != null) {
            throw new IllegalArgumentException("verificationDigit only applies to NIT");
        }
        if (isNit(documentType) && providedDigit != null && !Objects.equals(calculatedDigit, providedDigit)) {
            throw new IllegalArgumentException("verificationDigit does not match NIT documentNumber");
        }
    }

    private static void validateName(PersonType personType, String fullName, String businessName) {
        if (personType == PersonType.NATURAL && fullName == null) {
            throw new IllegalArgumentException("fullName is required for NATURAL personType");
        }
        if (personType == PersonType.JURIDICA && businessName == null) {
            throw new IllegalArgumentException("businessName is required for JURIDICA personType");
        }
    }

    private static boolean isSimpleNaturalCustomer(PersonType personType, Set<ThirdPartyRole> roles) {
        return personType == PersonType.NATURAL && roles.size() == 1 && roles.contains(ThirdPartyRole.CUSTOMER);
    }

    private static void validateSimpleNaturalCustomer(Integer identificationTypeCode, String businessName,
            String tradeName) {
        if (isNit(identificationTypeCode)) {
            throw new IllegalArgumentException("NIT is not allowed for simple NATURAL customer");
        }
        if (businessName != null) {
            throw new IllegalArgumentException("businessName is not allowed for simple NATURAL customer");
        }
        if (tradeName != null) {
            throw new IllegalArgumentException("tradeName is not allowed for simple NATURAL customer");
        }
    }

    private static Set<String> normalizeSimpleNaturalCustomerResponsibilities(Set<String> taxResponsibilities) {
        Set<String> normalized = taxResponsibilities == null || taxResponsibilities.isEmpty()
                ? Set.of(SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY)
                : TaxResponsibilityCatalog.normalize(taxResponsibilities);
        if (!normalized.equals(Set.of(SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY))) {
            throw new IllegalArgumentException("simple NATURAL customer must use R-99-PN tax responsibility");
        }
        return normalized;
    }

    private static TaxRegime normalizeSimpleNaturalCustomerRegime(TaxRegime taxRegime) {
        if (taxRegime == null) {
            return TaxRegime.NO_RESPONSABLE_IVA;
        }
        if (taxRegime != TaxRegime.NO_RESPONSABLE_IVA) {
            throw new IllegalArgumentException("simple NATURAL customer must use NO_RESPONSABLE_IVA tax regime");
        }
        return taxRegime;
    }

    private static Set<ThirdPartyRole> normalizeRoles(Set<ThirdPartyRole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("roles are required");
        }
        EnumSet<ThirdPartyRole> normalized = EnumSet.copyOf(roles);
        return Collections.unmodifiableSet(normalized);
    }

    private static String normalizeRequired(String value, int maxLength, String fieldName) {
        String normalized = normalizeOptional(value, maxLength, fieldName);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private static String normalizeOptional(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " length is invalid");
        }
        return normalized;
    }
}
