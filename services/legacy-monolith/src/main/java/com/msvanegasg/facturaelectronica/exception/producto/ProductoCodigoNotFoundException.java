package com.msvanegasg.facturaelectronica.exception.producto;

public class ProductoCodigoNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductoCodigoNotFoundException(Long codigoBarras) {
        super("Producto con Codigo de Barras: " + codigoBarras + " no encontrado");
    }
}
