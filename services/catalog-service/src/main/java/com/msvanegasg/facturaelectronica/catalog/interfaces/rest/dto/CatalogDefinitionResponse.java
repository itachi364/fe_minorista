package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

public record CatalogDefinitionResponse(
        String code,
        String label,
        String description,
        boolean regulatory,
        boolean companyConfigurable,
        boolean globalEditableByRoot,
        boolean active,
        int sortOrder) {
}
