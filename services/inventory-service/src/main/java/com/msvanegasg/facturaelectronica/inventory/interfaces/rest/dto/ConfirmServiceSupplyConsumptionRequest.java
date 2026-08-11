package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ConfirmServiceSupplyConsumptionRequest(
        @NotNull UUID serviceProductId,
        @NotNull UUID sourceDocumentId,
        @NotBlank String reason,
        @NotEmpty List<@Valid Line> lines) {

    public record Line(
            @NotNull UUID supplyProductId,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity) {
    }
}
