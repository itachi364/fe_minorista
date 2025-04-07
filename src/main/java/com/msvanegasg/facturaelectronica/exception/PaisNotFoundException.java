package com.msvanegasg.facturaelectronica.exception;

public class PaisNotFoundException extends RuntimeException {
    public PaisNotFoundException(String codigo) {
        super("País con código '" + codigo + "' no encontrado.");
    }
}
