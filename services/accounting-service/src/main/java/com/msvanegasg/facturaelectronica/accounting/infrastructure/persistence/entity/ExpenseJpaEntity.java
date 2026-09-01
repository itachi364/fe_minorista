package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

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
@Table(name = "accounting_expense")
public class ExpenseJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "supplier_id")
    private UUID supplierId;
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false)
    private ExpenseType expenseType;
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    @Column(nullable = false, length = 250)
    private String concept;
    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(name = "tax_total", nullable = false)
    private BigDecimal taxTotal;
    @Column(nullable = false)
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_condition", nullable = false)
    private PaymentCondition paymentCondition;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
}
