package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra", nullable = false)
    @NotNull
    private Compra compra;

    @Column(name = "id_producto", nullable = false)
    private Long producto;

    @Column(name = "cantidad", nullable = false)
    @NotNull
    @Positive
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @Column(name = "iva", nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal iva;

    @Column(name = "total_linea", nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal totalLinea;
    
    @Column(nullable = false)
    private Boolean activo;

}
