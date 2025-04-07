package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDocumentoDTO {
	
	@NotNull
	private Long codigo;

    @NotBlank
    private String nombre;

    private String descripcion;
}