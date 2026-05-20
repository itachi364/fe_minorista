package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Column(name = "descripcion", length = 250)
    @Size(max = 250)
    private String descripcion;

    @Column(name = "precio_base", nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal precioBase;

    @Column(name = "cantidad_stock", nullable = false)
    @NotNull
    @PositiveOrZero
    private Integer cantidadStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    @NotNull
    private CategoryJpaEntity categoria;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;

    @Column(name = "codigo_barras", nullable = false)
    @NotNull
    private Long codigoBarras;
}
