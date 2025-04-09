package com.msvanegasg.facturaelectronica.exception.tipodocumento;

public class TipoDocumentoNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public TipoDocumentoNotFoundException(Long id) {
        super("Tipo de documento con ID " + id + " no encontrado");
    }
}
