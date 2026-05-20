package com.msvanegasg.facturaelectronica.client;

public record DocumentTypeClientResponse(
        Long codigo,
        String nombre,
        String descripcion,
        Boolean activo) {
}
