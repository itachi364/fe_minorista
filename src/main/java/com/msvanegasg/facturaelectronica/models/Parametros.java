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

@Entity
@Table(name = "parametros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    @Column(name = "clave")
    private String clave;

    @Column(name = "valor")
    private String valor;

    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "activo")
    private Boolean activo;
}

