package com.msvanegasg.facturaelectronica.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @NotNull
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    @NotNull
    private Long numeroDocumento;
    
    @Column(name = "digito_verificacion")
    private Integer digitoVerificacion;

    @Column(name = "direccion", length = 150)
    @Size(max = 150)
    private String direccion;

    @Column(name = "telefono", length = 15)
    @Size(max = 15)
    private String telefono;

    @Column(name = "correo_electronico", length = 100)
    @Email
    @Size(max = 100)
    private String correoElectronico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false, length = 20)
    @NotNull
    private TipoCliente tipoCliente;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
    

    public enum TipoCliente {
        NATURAL,
        JURIDICO
    }
}
