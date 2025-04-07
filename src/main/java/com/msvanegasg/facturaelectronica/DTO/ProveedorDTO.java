package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorDTO {

    @NotNull
    private Long idTipoDocumento;

    @NotNull
    private Long numeroDocumento;

    @NotBlank
    private String nombre;

    private String telefono;

    private String direccion;

    @Email
    private String correo;
}
