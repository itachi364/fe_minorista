package com.msvanegasg.facturaelectronica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "pais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    @Column(name = "codigo_pais", length = 10)
    private String codigoPais; // Ej: "CO", "US", "MX"

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "moneda", length = 50)
    private String moneda;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
