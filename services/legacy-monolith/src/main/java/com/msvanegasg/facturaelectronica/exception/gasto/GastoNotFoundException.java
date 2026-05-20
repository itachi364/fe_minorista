package com.msvanegasg.facturaelectronica.exception.gasto;

public class GastoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public GastoNotFoundException(Long idGasto) {
        super("Gasto no encontrado con id: " + idGasto);
    }
}
