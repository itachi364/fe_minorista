package com.msvanegasg.facturaelectronica.exception;

public class ProveedorNotFoundException extends RuntimeException {
    public ProveedorNotFoundException(Long id) {
        super("Proveedor con ID " + id + " no encontrado");
    }
}
