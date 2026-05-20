package com.msvanegasg.facturaelectronica.exception.util;

public class DigitoVerificacionNoModificableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public DigitoVerificacionNoModificableException(String entidad) {
        super("El dígito de verificación del " + entidad + " no puede ser modificado.");
    }
}

