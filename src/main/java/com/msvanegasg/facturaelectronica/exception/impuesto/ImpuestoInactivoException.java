package com.msvanegasg.facturaelectronica.exception.impuesto;

public class ImpuestoInactivoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ImpuestoInactivoException(Long idImpuesto) {
        super("El Impuesto con id: " + idImpuesto + " se encuentra inactivo");
    }
}
