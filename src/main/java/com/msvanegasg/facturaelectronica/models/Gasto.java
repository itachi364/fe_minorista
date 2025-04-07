package com.msvanegasg.facturaelectronica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Gasto {

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
    private TipoGasto tipoGasto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    @NotNull
    private MetodoPago metodoPago;

    @Column(name = "url_evidencia", length = 255)
    private String urlEvidencia;

    @Column(name = "estado", nullable = false, length = 20)
    @NotBlank
    private String estado;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean activo;
}
