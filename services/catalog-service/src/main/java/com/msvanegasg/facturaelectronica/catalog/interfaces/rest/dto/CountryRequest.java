package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequest {

    private String codigoPais;
    private String nombre;
    private String moneda;
}
