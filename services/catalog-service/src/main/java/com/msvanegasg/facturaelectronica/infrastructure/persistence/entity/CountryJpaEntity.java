package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryJpaEntity {

    @Id
    @Column(name = "codigo_pais", length = 10)
    private String codigoPais;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "moneda", length = 50)
    private String moneda;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
