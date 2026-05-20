package com.msvanegasg.facturaelectronica.exception.producto;

public class ProductoIdNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductoIdNotFoundException(Long id) {
        super("Producto con ID: " + id + " no encontrado");
    }
}
