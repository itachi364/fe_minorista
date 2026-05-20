package com.msvanegasg.facturaelectronica.exception.tipodocumento;

public class TipoDocumentoNoModificableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public TipoDocumentoNoModificableException(Long numeroDocumento) {
        super("No se puede modificar el tipo de documento del número de documento: " + numeroDocumento);
    }
}
