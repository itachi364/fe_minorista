package com.msvanegasg.facturaelectronica.identity.application.usecase;

public class LicenseBlockedException extends RuntimeException {

    public LicenseBlockedException(String message) {
        super(message);
    }
}
