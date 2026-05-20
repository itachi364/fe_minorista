package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto;

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
public class CustomerResponse {

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
