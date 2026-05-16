package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseLineRequest {

    @NotNull
    private Long codigoBarras;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    private BigDecimal precioUnitario;

    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal iva;

    @NotNull
    @PositiveOrZero
    private BigDecimal totalLinea;
}
