package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.time.LocalDate;

public record CatalogItemCommand(
        String code,
        String label,
        String description,
        boolean regulatory,
        String source,
        String sourceVersion,
        LocalDate validFrom,
        LocalDate validTo,
        int sortOrder) {
}
