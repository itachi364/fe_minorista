package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

public class DianCertificateExpiredException extends RuntimeException {

    public DianCertificateExpiredException(String message) {
        super(message);
    }
}
