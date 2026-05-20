package com.msvanegasg.facturaelectronica.exception;

public class ParametroNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ParametroNotFoundException(Long id) {
        super("Parámetro con ID " + id + " no encontrado");
    }
}
