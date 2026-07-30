package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

import java.math.BigDecimal;

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
public class ProductResponse {

    private Long id;

    private String nombre;

    private String descripcion;

    private BigDecimal precioBase;

    private Integer cantidadStock;

    private CategorySummary categoria;

    private Long codigoBarras;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {

        private Long id;

        private String nombre;
    }
}
