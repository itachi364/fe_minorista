package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TaxRequest {

    @NotBlank
    private String nombre;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal porcentaje;

    @NotBlank
    private String tipo;

    @NotNull
    private String codPais;

    @NotNull
    private String descripcion;
}
