package com.msvanegasg.facturaelectronica.DTO.response;

import com.msvanegasg.facturaelectronica.enums.Estado;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoResponseDTO {

    private LocalDateTime fecha;
    private BigDecimal monto;
    private String descripcion;
    private TipoGastoResponseDTO tipoGasto;
    private MetodoPagoResponseDTO metodoPago;
    private String urlEvidencia;
    private Estado estado;
}
