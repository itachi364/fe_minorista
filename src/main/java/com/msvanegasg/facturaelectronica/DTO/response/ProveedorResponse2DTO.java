package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse2DTO {
	private Long idProveedor;
	private String nombre;
	private String numeroDocumento;
	private Long tipoDocumentoId;
	private String direccion;
	private String telefono;
	private String correoElectronico;
}
