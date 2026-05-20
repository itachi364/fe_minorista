package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;

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
@Table(name = "accounting_rule_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingRuleLineJpaEntity {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "line_order", nullable = false)
    private Integer lineOrder;

    @Column(name = "account_code", nullable = false, length = 30)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingEntrySide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_type", nullable = false, length = 30)
    private AccountingAmountType amountType;

    @Column(length = 250)
    private String description;
}
