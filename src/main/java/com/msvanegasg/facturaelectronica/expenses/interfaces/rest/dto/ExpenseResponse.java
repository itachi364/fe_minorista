package com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.enums.Estado;

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
public class ExpenseResponse {

    private LocalDateTime fecha;
    private BigDecimal monto;
    private String descripcion;
    private ExpenseTypeResponse tipoGasto;
    private PaymentMethodResponse metodoPago;
    private String urlEvidencia;
    private Estado estado;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpenseTypeResponse {

        private Long id;
        private String nombre;
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodResponse {

        private Long id;
        private String nombre;
        private String descripcion;
    }
}
