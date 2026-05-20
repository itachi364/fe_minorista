package com.msvanegasg.facturaelectronica.exception;

public class TipoGastoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public TipoGastoNotFoundException(Long id) {
        super("Tipo de gasto con ID " + id + " no encontrado");
    }
}
