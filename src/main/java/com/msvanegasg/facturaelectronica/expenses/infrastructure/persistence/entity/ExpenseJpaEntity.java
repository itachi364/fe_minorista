package com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ExpenseTypeJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.PaymentMethodJpaEntity;
import com.msvanegasg.facturaelectronica.enums.Estado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ExpenseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto")
    private Long idGasto;

    @Column(name = "fecha", nullable = false)
    @NotNull
    private LocalDateTime fecha;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_gasto", nullable = false)
    @NotNull
    private ExpenseTypeJpaEntity tipoGasto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    @NotNull
    private PaymentMethodJpaEntity metodoPago;

    @Column(name = "url_evidencia", length = 255)
    private String urlEvidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @NotNull
    private Estado estado;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
