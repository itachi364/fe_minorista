package com.msvanegasg.facturaelectronica.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoDTO {

    private Long codigo;
    private String nombre;
    private String descripcion;
}

