package com.msvanegasg.facturaelectronica.exception;

public class ImpuestoNotFoundException extends RuntimeException {

    public ImpuestoNotFoundException(Long id) {
        super("Impuesto no encontrado con ID: " + id);
    }

    public ImpuestoNotFoundException(String message) {
        super(message);
    }
}
