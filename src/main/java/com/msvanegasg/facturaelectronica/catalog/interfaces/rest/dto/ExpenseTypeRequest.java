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
public class ExpenseTypeRequest {

    @NotBlank
    private String nombre;

    private String descripcion;
}
