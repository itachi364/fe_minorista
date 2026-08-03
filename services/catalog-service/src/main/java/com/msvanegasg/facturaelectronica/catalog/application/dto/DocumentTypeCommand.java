package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record DocumentTypeCommand(
        Integer code,
        String name,
        String description) {
}
