package com.msvanegasg.facturaelectronica.exception.compra;

public class CompraNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompraNotFoundException(Long id) {
        super("Compra no encontrada con ID: " + id);
    }

    public CompraNotFoundException(String mensaje) {
        super(mensaje);
    }
}
