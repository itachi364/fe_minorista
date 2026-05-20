package com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.enums.Estado;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ExpenseRequest {

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
