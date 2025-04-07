package com.msvanegasg.facturaelectronica.exception;

public class ParametroNotFoundException extends RuntimeException {
    public ParametroNotFoundException(Long id) {
        super("Parámetro con ID " + id + " no encontrado");
    }
}
