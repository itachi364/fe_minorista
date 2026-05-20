package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.enums.Estado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PurchaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @Column(name = "id_proveedor", nullable = false)
    private Long idProveedor;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "iva_total")
    private BigDecimal ivaTotal;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "url_evidencia")
    private String urlEvidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @NotNull
    private Estado estado;

    @Column(name = "activo")
    private Boolean activo;
}
