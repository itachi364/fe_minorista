package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CustomerRequest {

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
