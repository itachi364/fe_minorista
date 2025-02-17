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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_accesos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAccesos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long idRegistro;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuarios usuario;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "direccion_ip")
    private String direccionIp;

    @Column(name = "accion")
    private String accion;

    @Column(name = "detalle")
    private String detalle;
}
