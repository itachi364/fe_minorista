package com.msvanegasg.facturaelectronica.exception.util;

public class NitInvalidoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public NitInvalidoException(Long numeroDocumento) {
        super("El número de documento " + numeroDocumento + " no es válido como NIT para persona jurídica.");
    }
}