package com.msvanegasg.facturaelectronica.exception;

public class TipoClienteInvalidoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public TipoClienteInvalidoException(String tipo) {
        super("Tipo de cliente inválido: " + tipo + ". Debe ser NATURAL o JURIDICO.");
    }
}