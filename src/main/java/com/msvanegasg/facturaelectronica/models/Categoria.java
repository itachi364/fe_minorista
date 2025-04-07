package com.msvanegasg.facturaelectronica.models;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    @Size(max = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
