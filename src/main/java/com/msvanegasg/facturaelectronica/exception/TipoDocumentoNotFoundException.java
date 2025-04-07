package com.msvanegasg.facturaelectronica.exception;

public class TipoDocumentoNotFoundException extends RuntimeException {
    public TipoDocumentoNotFoundException(Long id) {
        super("Tipo de documento con ID " + id + " no encontrado");
    }
}
