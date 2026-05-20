package com.msvanegasg.facturaelectronica.exception.impuesto;

public class TipoImpuestoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public TipoImpuestoNotFoundException(String tipo) {
        super("Tipo de impuesto no encontrado con nombre: " + tipo);
    }

}
