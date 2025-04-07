package com.msvanegasg.facturaelectronica.exception;

public class ClienteAlreadyExistsException extends RuntimeException {
    public ClienteAlreadyExistsException(Long numeroDocumento) {
        super("Ya existe un cliente con el número de documento: " + numeroDocumento);
    }
}
