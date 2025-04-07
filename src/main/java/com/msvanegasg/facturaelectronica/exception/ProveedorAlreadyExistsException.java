package com.msvanegasg.facturaelectronica.exception;

public class ProveedorAlreadyExistsException extends RuntimeException {
    public ProveedorAlreadyExistsException(Long numeroDocumento) {
        super("Ya existe un Proveedor con el número de documento: " + numeroDocumento);
    }
}
