package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Long idCliente;
    private String nombre;

    private Long idTipoDocumento;
    private String codigoTipoDocumento;
    private String descripcionTipoDocumento;

    private Long numeroDocumento;
    private Integer digitoVerificacion;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private String tipoCliente;
    private Boolean activo;

}
