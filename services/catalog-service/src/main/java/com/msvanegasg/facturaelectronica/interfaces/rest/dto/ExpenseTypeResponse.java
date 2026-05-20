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
public class ExpenseTypeResponse {

    private Long idTipoGasto;

    private String nombre;

    private String descripcion;

    private Boolean activo;
}
