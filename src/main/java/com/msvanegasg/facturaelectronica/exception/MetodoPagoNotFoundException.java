package com.msvanegasg.facturaelectronica.exception;

public class MetodoPagoNotFoundException extends RuntimeException {
    public MetodoPagoNotFoundException(Long id) {
        super("Método de pago con ID " + id + " no encontrado");
    }
}
