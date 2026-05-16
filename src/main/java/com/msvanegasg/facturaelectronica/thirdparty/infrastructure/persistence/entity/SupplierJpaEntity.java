package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DocumentTypeJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SupplierJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @NotNull
    private DocumentTypeJpaEntity tipoDocumento;

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

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
