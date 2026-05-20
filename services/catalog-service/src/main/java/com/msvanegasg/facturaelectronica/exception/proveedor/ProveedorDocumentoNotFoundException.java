package com.msvanegasg.facturaelectronica.exception.proveedor;

public class ProveedorDocumentoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ProveedorDocumentoNotFoundException(Long numeroDocumento, Long tipoDocumento) {
        super("Proveedor con Numero de documento " + numeroDocumento + " y tipo de Documento: "+tipoDocumento+" no encontrado");
    }
}
