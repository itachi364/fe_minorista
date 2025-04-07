package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoGastoDTO {

    @NotBlank
    private String nombre;

    private String descripcion;
}
