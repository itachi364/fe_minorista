package com.msvanegasg.facturaelectronica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "iva_total")
    private BigDecimal ivaTotal;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "url_evidencia")
    private String urlEvidencia;

    @Column(name = "estado")
    private String estado;
    
    @Column(name = "activo")
    private Boolean activo;
}

