package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 250)
    private String descripcion;

    @NotNull
    @PositiveOrZero
    private BigDecimal precioBase;

    @NotNull
    @PositiveOrZero
    private Integer cantidadStock;

    @NotNull
    private Long idCategoria;

    @NotNull
    private Long codigoBarras;
}
