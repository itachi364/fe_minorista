package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tipodocumento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class DocumentTypeJpaEntity {

    @Id
    @Column(name = "codigo", nullable = false, unique = true)
    private Integer codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    @NotBlank
    @Size(max = 50)
    private String nombre;

    @Column(name = "descripcion", length = 100)
    @Size(max = 100)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
