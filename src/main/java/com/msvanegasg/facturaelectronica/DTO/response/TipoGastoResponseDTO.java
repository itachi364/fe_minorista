package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoGastoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
}
