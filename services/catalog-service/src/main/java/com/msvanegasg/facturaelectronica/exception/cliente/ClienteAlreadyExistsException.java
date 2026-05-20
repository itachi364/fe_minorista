package com.msvanegasg.facturaelectronica.exception.cliente;

public class ClienteAlreadyExistsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ClienteAlreadyExistsException(Long numeroDocumento, Long tipoDocumento) {
        super("Ya existe un cliente con el número de documento: " + numeroDocumento + " con tipo de documento: " + tipoDocumento);
    }
}
