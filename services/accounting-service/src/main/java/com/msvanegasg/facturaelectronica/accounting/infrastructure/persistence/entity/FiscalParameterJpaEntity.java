package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fiscal_parameter")
public class FiscalParameterJpaEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 40) private String code;
    @Column(nullable = false, length = 40) private String version;
    @Column(nullable = false, precision = 38, scale = 6) private BigDecimal value;
    @Column(name = "valid_from", nullable = false) private LocalDate validFrom;
    @Column(name = "valid_to") private LocalDate validTo;
    @Column(name = "legal_reference", nullable = false, length = 250) private String legalReference;
    @Column(name = "source_url", nullable = false, length = 500) private String sourceUrl;
    @Column(nullable = false) private Boolean published;
}
