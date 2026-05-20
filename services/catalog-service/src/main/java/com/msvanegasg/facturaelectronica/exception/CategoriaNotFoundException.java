package com.msvanegasg.facturaelectronica.exception;

public class CategoriaNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public CategoriaNotFoundException(Long id) {
        super("La categoría con ID " + id + " no fue encontrada.");
    }
}
