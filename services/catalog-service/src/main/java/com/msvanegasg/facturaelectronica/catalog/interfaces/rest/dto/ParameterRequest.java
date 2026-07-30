package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ParameterRequest {

    @NotBlank
    private String clave;

    @NotBlank
    private String valor;

    private String descripcion;
}
