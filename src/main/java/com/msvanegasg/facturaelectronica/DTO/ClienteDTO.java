package com.msvanegasg.facturaelectronica.DTO;

import java.util.Optional;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    @NotBlank
    private String nombre;

    @NotNull
    private Long idTipoDocumento;

    @NotNull
    private Long numeroDocumento;

    private Optional<Integer> digitoVerificacion = Optional.empty();

    @Size(max = 150)
    private String direccion;

    @Size(max = 15)
    private String telefono;

    @Email
    @Size(max = 100)
    private String correoElectronico;

}
