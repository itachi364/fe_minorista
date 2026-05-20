package com.msvanegasg.facturaelectronica.exception;

public class PaisNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public PaisNotFoundException(String codigo) {
        super("País con código '" + codigo + "' no encontrado.");
    }
}
