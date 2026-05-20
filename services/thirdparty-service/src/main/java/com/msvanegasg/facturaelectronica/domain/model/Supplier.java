package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;

public final class Supplier {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_ADDRESS_LENGTH = 150;
    private static final int MAX_PHONE_LENGTH = 15;
    private static final int MAX_EMAIL_LENGTH = 100;

    private final Long id;
    private final String name;
    private final DocumentTypeSummary documentType;
    private final Long documentNumber;
    private final Integer verificationDigit;
    private final String address;
    private final String phone;
    private final String email;
    private final boolean active;

    private Supplier(Long id, String name, DocumentTypeSummary documentType, Long documentNumber,
            Integer verificationDigit, String address, String phone, String email, boolean active) {
        this.id = id;
        this.name = name;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.verificationDigit = verificationDigit;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }

    public static Supplier create(String name, DocumentTypeSummary documentType, Long documentNumber,
            Integer verificationDigit, String address, String phone, String email) {
        return new Supplier(null, normalizeName(name), requireDocumentType(documentType),
                requireLong(documentNumber, "documentNumber"), verificationDigit,
                normalizeOptional(address, MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"),
                normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                true);
    }

    public static Supplier restore(Long id, String name, DocumentTypeSummary documentType, Long documentNumber,
            Integer verificationDigit, String address, String phone, String email, boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Supplier(id, normalizeName(name), requireDocumentType(documentType),
                requireLong(documentNumber, "documentNumber"), verificationDigit,
                normalizeOptional(address, MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"),
                normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                active);
    }

    public Supplier updateProfile(String name, Integer verificationDigit, String address, String phone, String email) {
        return new Supplier(id, normalizeName(name), documentType, documentNumber, verificationDigit,
                normalizeOptional(address, MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"),
                normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                active);
    }

    public Supplier enable() {
        return new Supplier(id, name, documentType, documentNumber, verificationDigit, address, phone, email, true);
    }

    public Supplier disable() {
        return new Supplier(id, name, documentType, documentNumber, verificationDigit, address, phone, email, false);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public DocumentTypeSummary documentType() {
        return documentType;
    }

    public Long documentNumber() {
        return documentNumber;
    }

    public Integer verificationDigit() {
        return verificationDigit;
    }

    public String address() {
        return address;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public boolean active() {
        return active;
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must be 100 characters or less");
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

    private static Long requireLong(Long value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " is required");
    }

    private static DocumentTypeSummary requireDocumentType(DocumentTypeSummary value) {
        return Objects.requireNonNull(value, "documentType is required");
    }
}
