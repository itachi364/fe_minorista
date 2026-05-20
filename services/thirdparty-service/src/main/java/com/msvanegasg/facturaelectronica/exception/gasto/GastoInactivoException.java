package com.msvanegasg.facturaelectronica.exception.gasto;

public class GastoInactivoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public GastoInactivoException(Long idGasto) {
        super("El gasto con id: " + idGasto + " se encuentra inactivo");
    }
}
