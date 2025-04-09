package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 250)
    private String descripcion;

    @NotNull
    @PositiveOrZero
    private BigDecimal precioBase;

    @NotNull
    @PositiveOrZero
    private Integer cantidadStock;

    @NotNull
    private Long idCategoria;
}
