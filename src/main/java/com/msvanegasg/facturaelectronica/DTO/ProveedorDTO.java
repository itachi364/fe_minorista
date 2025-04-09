package com.msvanegasg.facturaelectronica.DTO;

import java.util.Optional;

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
    
    private Optional<Integer> digitoVerificacion = Optional.empty();

    @NotBlank
    private String nombre;

    private String telefono;

    private String direccion;

    @Email
    private String correo;
}
