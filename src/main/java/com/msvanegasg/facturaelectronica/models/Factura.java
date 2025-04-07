package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    @Column(name = "fecha", nullable = false)
    @NotNull
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private Cliente cliente;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @Column(name = "iva_total", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal ivaTotal;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metodo_pago", nullable = false)
    @NotNull
    private MetodoPago metodoPago;

    @Column(name = "estado", length = 20, nullable = false)
    @NotBlank
    private String estado;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
