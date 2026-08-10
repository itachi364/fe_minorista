package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record CatalogItemActivationRequest(@NotNull Boolean active) {
}
