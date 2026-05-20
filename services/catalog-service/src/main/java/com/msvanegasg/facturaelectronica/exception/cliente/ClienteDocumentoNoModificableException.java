package com.msvanegasg.facturaelectronica.exception.cliente;

public class ClienteDocumentoNoModificableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ClienteDocumentoNoModificableException(Long numeroDocumento) {
        super("No se puede modificar el número de documento del cliente con número: " + numeroDocumento);
    }
}
