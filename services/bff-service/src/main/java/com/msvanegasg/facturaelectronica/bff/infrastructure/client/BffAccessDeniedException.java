package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

public class BffAccessDeniedException extends RuntimeException {

    public BffAccessDeniedException(String message) {
        super(message);
    }
}
