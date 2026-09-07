package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "withholding_calculation_snapshot")
public class WithholdingCalculationSnapshotJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private AccountingSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "third_party_id", nullable = false)
    private UUID thirdPartyId;

    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "withholding_type", nullable = false, length = 30)
    private WithholdingType withholdingType;

    @Column(name = "base_amount", nullable = false)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "rule_version", length = 40)
    private String ruleVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WithholdingDecision decision;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
