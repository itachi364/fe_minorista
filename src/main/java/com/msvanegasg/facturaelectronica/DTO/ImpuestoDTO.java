package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpuestoDTO {

    @NotBlank
    private String nombre;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal porcentaje;

    @NotBlank
    private String tipo;

    @NotNull
    private String codPais;
    
    @NotNull
    private String descripcion;
}

