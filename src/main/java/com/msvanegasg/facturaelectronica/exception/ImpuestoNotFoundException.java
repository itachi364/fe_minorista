package com.msvanegasg.facturaelectronica.exception;

public class ImpuestoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ImpuestoNotFoundException(Long id) {
        super("Impuesto no encontrado con ID: " + id);
    }

    public ImpuestoNotFoundException(String message) {
        super(message);
    }
}
