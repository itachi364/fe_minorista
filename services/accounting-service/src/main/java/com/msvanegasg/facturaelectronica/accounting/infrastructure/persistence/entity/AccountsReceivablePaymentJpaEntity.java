package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
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
@Table(name = "accounting_accounts_receivable_payment")
public class AccountsReceivablePaymentJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "accounts_receivable_id", nullable = false)
    private UUID accountsReceivableId;
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(name = "payment_method", nullable = false, length = 80)
    private String paymentMethod;
    @Column(length = 120)
    private String reference;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}