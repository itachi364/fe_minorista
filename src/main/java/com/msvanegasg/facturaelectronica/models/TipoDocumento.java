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
@Table(name = "tipodocumento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDocumento {

	@Id
	@Column(name = "codigo")
	private Long codigo;
	
	@Column(name = "nombre")
	private String nombre;

	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "activo")
    private Boolean activo;
}