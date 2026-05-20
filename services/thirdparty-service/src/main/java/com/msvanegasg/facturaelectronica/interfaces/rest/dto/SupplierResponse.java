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
public class SupplierResponse {

    private Long idProveedor;
    private String nombre;
    private DocumentTypeResponse tipoDocumento;
    private Long numeroDocumento;
    private Integer digitoVerificacion;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private Boolean activo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentTypeResponse {

        private Long id;
        private String nombre;
    }
}
