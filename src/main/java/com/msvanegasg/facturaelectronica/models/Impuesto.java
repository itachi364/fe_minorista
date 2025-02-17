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

import java.math.BigDecimal;


@Entity
@Table(name = "impuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_impuesto")
    private Long idImpuesto;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "porcentaje")
    private BigDecimal porcentaje;

    @Column(name = "tipo")
    private String tipo; // Ej: IVA, Retención, etc.
    
    @Column(name = "activo")
    private Boolean activo;
}
