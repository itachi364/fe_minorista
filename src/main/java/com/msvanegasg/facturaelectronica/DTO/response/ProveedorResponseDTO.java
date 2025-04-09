package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponseDTO {

    private Long idProveedor;
    private String nombre;
    private TipoDocumentoResponseDTO tipoDocumento;
    private Long numeroDocumento;
    private Integer digitoVerificacion;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private Boolean activo;
}

