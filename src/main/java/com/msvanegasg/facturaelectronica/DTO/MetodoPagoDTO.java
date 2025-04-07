package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPagoDTO {

    @NotBlank
    private String nombre;

    private String descripcion;
}
