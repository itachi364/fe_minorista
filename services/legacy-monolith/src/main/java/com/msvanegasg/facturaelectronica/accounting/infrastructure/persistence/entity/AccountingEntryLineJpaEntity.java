package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounting_entry_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingEntryLineJpaEntity {

    @Id
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "line_order", nullable = false)
    private Integer lineOrder;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_code", nullable = false, length = 30)
    private String accountCode;

    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Column(name = "thirdparty_id")
    private UUID thirdpartyId;

    @Column(name = "debit_amount", nullable = false)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", nullable = false)
    private BigDecimal creditAmount;

    @Column(length = 250)
    private String description;
}
