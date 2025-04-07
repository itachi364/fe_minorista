package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "tipodocumento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class TipoDocumento {

    @Id
    @Column(name = "codigo",nullable = false, unique = true)
    private Long codigo;

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
