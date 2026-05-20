package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import java.math.BigDecimal;

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
public class TaxUpdateResponse {

    private Long id;

    private String nombre;

    private BigDecimal porcentaje;

    private String tipo;

    private String descripcion;

    private CountryResponse codigoPais;
}
