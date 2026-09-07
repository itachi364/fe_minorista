package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

public class DianInvalidCertificateException extends RuntimeException {

    public DianInvalidCertificateException(String message) {
        super(message);
    }

    public DianInvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
