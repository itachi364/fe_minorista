package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.enums.TipoClienteEnum;

public final class Customer {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_ADDRESS_LENGTH = 150;
    private static final int MAX_PHONE_LENGTH = 15;
    private static final int MAX_EMAIL_LENGTH = 100;

    private final Long id;
    private final String name;
    private final Long documentTypeId;
    private final Long documentNumber;
    private final Integer verificationDigit;
    private final String address;
    private final String phone;
    private final String email;
    private final TipoClienteEnum customerType;
    private final boolean active;

    private Customer(Long id, String name, Long documentTypeId, Long documentNumber, Integer verificationDigit,
            String address, String phone, String email, TipoClienteEnum customerType, boolean active) {
        this.id = id;
        this.name = name;
        this.documentTypeId = documentTypeId;
        this.documentNumber = documentNumber;
        this.verificationDigit = verificationDigit;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.customerType = customerType;
        this.active = active;
    }

    public static Customer create(String name, Long documentTypeId, Long documentNumber, Integer verificationDigit,
            String address, String phone, String email, TipoClienteEnum customerType) {
        return new Customer(null, normalizeName(name), requireLong(documentTypeId, "documentTypeId"),
                requireLong(documentNumber, "documentNumber"), verificationDigit, normalizeOptional(address,
                        MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"), normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                requireCustomerType(customerType), true);
    }

    public static Customer restore(Long id, String name, Long documentTypeId, Long documentNumber,
            Integer verificationDigit, String address, String phone, String email, TipoClienteEnum customerType,
            boolean active) {
        Objects.requireNonNull(id, "id is required");
        return new Customer(id, normalizeName(name), requireLong(documentTypeId, "documentTypeId"),
                requireLong(documentNumber, "documentNumber"), verificationDigit, normalizeOptional(address,
                        MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"), normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                requireCustomerType(customerType), active);
    }

    public Customer updateProfile(String name, Integer verificationDigit, String address, String phone, String email,
            TipoClienteEnum customerType) {
        return new Customer(id, normalizeName(name), documentTypeId, documentNumber, verificationDigit,
                normalizeOptional(address, MAX_ADDRESS_LENGTH, "address"),
                normalizeOptional(phone, MAX_PHONE_LENGTH, "phone"), normalizeOptional(email, MAX_EMAIL_LENGTH, "email"),
                requireCustomerType(customerType), active);
    }

    public Customer enable() {
        return new Customer(id, name, documentTypeId, documentNumber, verificationDigit, address, phone, email,
                customerType, true);
    }

    public Customer disable() {
        return new Customer(id, name, documentTypeId, documentNumber, verificationDigit, address, phone, email,
                customerType, false);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Long documentTypeId() {
        return documentTypeId;
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

    public TipoClienteEnum customerType() {
        return customerType;
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

    private static TipoClienteEnum requireCustomerType(TipoClienteEnum value) {
        return Objects.requireNonNull(value, "customerType is required");
    }
}
