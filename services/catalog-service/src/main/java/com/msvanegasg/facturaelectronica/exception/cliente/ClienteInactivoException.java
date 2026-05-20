package com.msvanegasg.facturaelectronica.exception.cliente;

public class ClienteInactivoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ClienteInactivoException(Long numeroDocumento) {
		super("El cliente con número de documento " + numeroDocumento + " está inactivo y no puede ser actualizado.");
	}
}
