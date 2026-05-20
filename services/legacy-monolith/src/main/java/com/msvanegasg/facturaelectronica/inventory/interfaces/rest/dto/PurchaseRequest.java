package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseRequest {

    @NotNull
    private Long numeroDocumento;

    @NotNull
    private Long tipoDocumentoId;

    private LocalDateTime fecha;

    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal ivaTotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal total;

    private String urlEvidencia;

    @NotEmpty
    private List<@Valid PurchaseLineRequest> detalles;
}
