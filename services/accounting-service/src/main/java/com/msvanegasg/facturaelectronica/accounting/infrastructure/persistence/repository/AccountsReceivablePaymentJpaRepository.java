package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsReceivablePaymentJpaEntity;

public interface AccountsReceivablePaymentJpaRepository extends JpaRepository<AccountsReceivablePaymentJpaEntity, UUID> {
}