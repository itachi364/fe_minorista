package com.msvanegasg.facturaelectronica.exception.compra;

public class CompraNoEditableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompraNoEditableException(Long id) {
        super("No se puede modificar la compra con ID " + id + " porque no está en estado PENDIENTE.");
    }
}

