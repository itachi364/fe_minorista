package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

public class ProviderSubmissionNotFoundException extends RuntimeException {

    public ProviderSubmissionNotFoundException(String trackingId) {
        super("No existe envio de proveedor con tracking ID " + trackingId + ".");
    }
}
