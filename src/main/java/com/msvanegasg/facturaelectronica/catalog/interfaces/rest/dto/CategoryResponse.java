package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CategoryResponse {

    private Long idCategoria;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
