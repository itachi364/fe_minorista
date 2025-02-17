package com.msvanegasg.facturaelectronica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "detalle_gasto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleGasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_gasto")
    private Long idDetalleGasto;

    @ManyToOne
    @JoinColumn(name = "id_gasto")
    private Gastos gasto;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "monto_unitario")
    private BigDecimal montoUnitario;

    @Column(name = "subtotal")
    private BigDecimal subtotal;
}

