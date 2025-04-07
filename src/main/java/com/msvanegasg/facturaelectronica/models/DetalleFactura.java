package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factura", nullable = false)
    @NotNull
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    @NotNull
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    @NotNull
    @Positive
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal iva;

    @Column(name = "total_linea", nullable = false, precision = 12, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal totalLinea;
}
