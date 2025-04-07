package com.msvanegasg.facturaelectronica.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponseDTO {

    private Long idProveedor;
    private String nombre;
    private Long tipoDocumento;
    private Long numeroDocumento;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private Boolean activo;
}

