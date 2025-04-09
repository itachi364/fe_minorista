package com.msvanegasg.facturaelectronica.DTO;

import com.msvanegasg.facturaelectronica.enums.Estado;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraDTO {

    @NotNull
    private Long idProveedor;

    private LocalDateTime fecha;

    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal ivaTotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal total;

    private String urlEvidencia;

    @NotNull
    private Estado estado;

    @NotEmpty
    private List<DetalleCompraDTO> detalles;
}
