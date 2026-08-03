package com.msvanegasg.facturaelectronica.exception.tipodocumento;

public class TipoDocumentoNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TipoDocumentoNotFoundException(Integer code) {
        super("Tipo de documento con codigo " + code + " no encontrado");
    }
}