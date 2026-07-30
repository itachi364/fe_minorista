package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_impuesto")
    private Long idImpuesto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "codigo_pais", referencedColumnName = "codigo_pais", nullable = false)
    private CountryJpaEntity pais;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;
}
