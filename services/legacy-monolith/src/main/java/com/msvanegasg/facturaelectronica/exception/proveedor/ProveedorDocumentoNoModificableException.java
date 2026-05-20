package com.msvanegasg.facturaelectronica.exception.proveedor;

public class ProveedorDocumentoNoModificableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ProveedorDocumentoNoModificableException(Long numeroDocumento) {
        super("No se puede modificar el número de documento del Proveedor con número: " + numeroDocumento);
    }
}
