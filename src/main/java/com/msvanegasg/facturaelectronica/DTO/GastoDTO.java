package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.msvanegasg.facturaelectronica.enums.Estado;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoDTO {

    @NotNull
    private LocalDateTime fecha;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monto;

    @Size(max = 255)
    private String descripcion;

    @NotNull
    private Long idTipoGasto;

    @NotNull
    private Long idMetodoPago;

    @Size(max = 255)
    private String urlEvidencia;

    @NotNull
    private Estado estado;
}
