package com.msvanegasg.facturaelectronica.exception.proveedor;

public class ProveedorAlreadyExistsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ProveedorAlreadyExistsException(Long numeroDocumento, Long tipoDocumento) {
        super("Ya existe un Proveedor con el número de documento: " + numeroDocumento+" con tipo de documento: "+tipoDocumento);
    }
}
