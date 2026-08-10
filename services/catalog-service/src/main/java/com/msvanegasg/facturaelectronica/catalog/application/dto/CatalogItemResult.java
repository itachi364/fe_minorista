package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.time.LocalDate;

public record CatalogItemResult(
        String catalogCode,
        String code,
        String label,
        String description,
        boolean active,
        boolean enabledForCompany,
        boolean regulatory,
        String source,
        String sourceVersion,
        LocalDate validFrom,
        LocalDate validTo,
        int sortOrder) {
}
