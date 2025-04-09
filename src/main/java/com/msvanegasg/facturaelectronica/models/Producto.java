package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Producto {

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
    private Categoria categoria;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
