package com.msvanegasg.facturaelectronica.exception;

public class MetodoPagoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public MetodoPagoNotFoundException(Long id) {
        super("Método de pago con ID " + id + " no encontrado");
    }
}
