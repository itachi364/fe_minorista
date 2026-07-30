package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

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
public class CountryResponse {

    private String codigoPais;
    private String nombre;
    private String moneda;
    private Boolean activo;
}
