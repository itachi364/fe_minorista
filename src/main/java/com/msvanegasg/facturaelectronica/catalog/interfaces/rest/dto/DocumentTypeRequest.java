package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

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
public class DocumentTypeRequest {

    @NotNull
    private Long codigo;

    @NotBlank
    private String nombre;

    private String descripcion;
}
