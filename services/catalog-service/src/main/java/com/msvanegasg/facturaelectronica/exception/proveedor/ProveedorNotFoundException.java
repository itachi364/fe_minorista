package com.msvanegasg.facturaelectronica.exception.proveedor;

public class ProveedorNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ProveedorNotFoundException(Long id) {
		super("Cliente no encontrado con Id: " + id+" no encontrado");
	}
}
