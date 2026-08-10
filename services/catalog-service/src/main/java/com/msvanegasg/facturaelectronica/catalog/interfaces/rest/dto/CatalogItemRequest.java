package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CatalogItemRequest(
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 180) String label,
        @Size(max = 300) String description,
        boolean regulatory,
        @NotBlank @Size(max = 80) String source,
        @NotBlank @Size(max = 40) String sourceVersion,
        LocalDate validFrom,
        LocalDate validTo,
        @NotNull Integer sortOrder) {
}
