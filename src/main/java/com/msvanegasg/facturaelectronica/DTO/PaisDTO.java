package com.msvanegasg.facturaelectronica.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaisDTO {
    private String codigoPais;
    private String nombre;
    private String moneda;
}
