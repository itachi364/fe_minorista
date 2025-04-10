package com.msvanegasg.facturaelectronica.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompraDTO {

	@NotBlank
	private Long codigoBarras;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    private BigDecimal precioUnitario;

    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    private BigDecimal iva;

    @NotNull
    @PositiveOrZero
    private BigDecimal totalLinea;
}
