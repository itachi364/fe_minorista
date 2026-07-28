package com.msvanegasg.facturaelectronica.billing.application.usecase;

public class LicenseBlockedException extends RuntimeException {

    public LicenseBlockedException(String message) {
        super(message);
    }
}
