package com.msvanegasg.facturaelectronica.bff.domain.model;

public class UnsupportedBffRouteException extends RuntimeException {

    public UnsupportedBffRouteException(String path) {
        super("La ruta no esta expuesta por el BFF: " + path);
    }
}
