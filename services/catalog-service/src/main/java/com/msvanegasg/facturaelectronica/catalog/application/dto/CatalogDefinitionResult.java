package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record CatalogDefinitionResult(
        String code,
        String label,
        String description,
        boolean regulatory,
        boolean companyConfigurable,
        boolean globalEditableByRoot,
        boolean active,
        int sortOrder) {
}
