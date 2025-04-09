package com.msvanegasg.facturaelectronica.models;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.enums.Estado;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Compra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_compra")
	private Long idCompra;

	@ManyToOne
	@JoinColumn(name = "id_proveedor")
	private Proveedor proveedor;

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
