package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotNull
    private Long idTipoDocumento;

    @NotNull
    private Long numeroDocumento;

    private Optional<Integer> digitoVerificacion;

    @Size(max = 150)
    private String direccion;

    @Size(max = 15)
    private String telefono;

    @Email
    @Size(max = 100)
    private String correoElectronico;
}