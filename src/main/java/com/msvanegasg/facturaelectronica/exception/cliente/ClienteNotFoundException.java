package com.msvanegasg.facturaelectronica.exception.cliente;

public class ClienteNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ClienteNotFoundException(Long numeroDocumento, Long tipoDocumento) {
		super("Cliente no encontrado con numero de documento: " + numeroDocumento + " con tipo de Documento: "
				+ tipoDocumento);
	}
}
