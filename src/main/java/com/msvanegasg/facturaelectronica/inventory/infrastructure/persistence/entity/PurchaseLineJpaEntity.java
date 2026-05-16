package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PurchaseLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra", nullable = false)
    @NotNull
    private PurchaseJpaEntity compra;

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
