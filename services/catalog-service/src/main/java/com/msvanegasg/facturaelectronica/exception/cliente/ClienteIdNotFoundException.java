package com.msvanegasg.facturaelectronica.exception.cliente;

public class ClienteIdNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ClienteIdNotFoundException(Long id) {
		super("Cliente no encontrado con id: " + id);
	}
}
