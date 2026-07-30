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
public class DocumentTypeResponse {

    private Long codigo;

    private String nombre;

    private String descripcion;

    private Boolean activo;
}
