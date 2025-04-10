package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Integer cantidadStock;
    private CategoriaResponseDTO categoria;
    private Long codigoBarras;
}
