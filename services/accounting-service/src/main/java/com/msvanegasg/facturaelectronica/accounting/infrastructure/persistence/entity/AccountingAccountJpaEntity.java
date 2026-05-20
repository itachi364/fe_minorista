package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountLevel;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;

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
@Table(name = "accounting_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingAccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccountCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccountLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountNature nature;

    @Column(name = "parent_account_id")
    private UUID parentAccountId;

    @Column(nullable = false)
    private Boolean active;
}
