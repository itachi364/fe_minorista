package com.msvanegasg.facturaelectronica.providerretry;

public class ProviderRetryStillFailedException extends RuntimeException {

    public ProviderRetryStillFailedException(String message) {
        super(message);
    }
}