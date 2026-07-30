package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
