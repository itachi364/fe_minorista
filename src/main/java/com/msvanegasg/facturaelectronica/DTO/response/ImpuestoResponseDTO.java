package com.msvanegasg.facturaelectronica.DTO.response;


import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.models.Pais;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpuestoResponseDTO {

    private Long id;
    private String nombre;
    private BigDecimal porcentaje;
    private String tipo;
    private String descripcion;
    private Pais codigoPais;
}
