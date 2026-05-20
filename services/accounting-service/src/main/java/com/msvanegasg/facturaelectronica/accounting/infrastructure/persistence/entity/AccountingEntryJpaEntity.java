package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounting_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingEntryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 250)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private AccountingSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingEntryStatus status;

    @Column(name = "debit_total", nullable = false)
    private BigDecimal debitTotal;

    @Column(name = "credit_total", nullable = false)
    private BigDecimal creditTotal;
}
