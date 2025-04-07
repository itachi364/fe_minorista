package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroDTO {

    @NotBlank
    private String clave;

    @NotBlank
    private String valor;

    private String descripcion;
}
