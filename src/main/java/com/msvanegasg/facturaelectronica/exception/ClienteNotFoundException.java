package com.msvanegasg.facturaelectronica.exception;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException(Long id) {
        super("Cliente no encontrado con id: " + id);
    }
}
