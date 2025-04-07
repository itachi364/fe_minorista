package com.msvanegasg.facturaelectronica.exception;

public class TipoGastoNotFoundException extends RuntimeException {
    public TipoGastoNotFoundException(Long id) {
        super("Tipo de gasto con ID " + id + " no encontrado");
    }
}
