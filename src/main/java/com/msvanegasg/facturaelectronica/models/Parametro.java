package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parametros", uniqueConstraints = {
    @UniqueConstraint(columnNames = "clave", name = "uk_parametro_clave")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
