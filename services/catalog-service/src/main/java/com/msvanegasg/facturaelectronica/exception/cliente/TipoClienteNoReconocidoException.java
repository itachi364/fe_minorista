package com.msvanegasg.facturaelectronica.exception.cliente;

public class TipoClienteNoReconocidoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TipoClienteNoReconocidoException(Long idTipoDocumento) {
        super("No se pudo determinar el tipo de cliente a partir del tipo de documento con id: " + idTipoDocumento);
    }
}
