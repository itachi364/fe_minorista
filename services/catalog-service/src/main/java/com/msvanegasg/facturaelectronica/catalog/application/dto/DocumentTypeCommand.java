package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record DocumentTypeCommand(
        Long code,
        String name,
        String description) {
}
