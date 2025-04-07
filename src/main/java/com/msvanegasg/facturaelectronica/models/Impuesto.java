package com.msvanegasg.facturaelectronica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_impuesto")
    private Long idImpuesto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo; // Ej: IVA, Retención, etc.

    @ManyToOne
    @JoinColumn(name = "codigo_pais", referencedColumnName = "codigo_pais", nullable = false)
    private Pais pais;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
    
    @Column(name= "descripcion", nullable = false)
    private String descripcion;
}
