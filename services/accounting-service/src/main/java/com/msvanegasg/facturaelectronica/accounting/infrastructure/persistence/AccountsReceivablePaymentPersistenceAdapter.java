package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivablePayment;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsReceivablePaymentJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountsReceivablePaymentJpaRepository;

@Component
public class AccountsReceivablePaymentPersistenceAdapter implements AccountsReceivablePaymentRepositoryPort {

    private final AccountsReceivablePaymentJpaRepository repository;

    public AccountsReceivablePaymentPersistenceAdapter(AccountsReceivablePaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountsReceivablePayment save(AccountsReceivablePayment payment) {
        return toDomain(repository.save(toEntity(payment)));
    }

    private static AccountsReceivablePayment toDomain(AccountsReceivablePaymentJpaEntity entity) {
        return new AccountsReceivablePayment(entity.getId(), entity.getCompanyId(), entity.getAccountsReceivableId(),
                entity.getPaymentDate(), entity.getAmount(), entity.getPaymentMethod(), entity.getReference(),
                entity.getCreatedBy(), entity.getCreatedAt());
    }

    private static AccountsReceivablePaymentJpaEntity toEntity(AccountsReceivablePayment payment) {
        AccountsReceivablePaymentJpaEntity entity = new AccountsReceivablePaymentJpaEntity();
        entity.setId(payment.id());
        entity.setCompanyId(payment.companyId());
        entity.setAccountsReceivableId(payment.accountsReceivableId());
        entity.setPaymentDate(payment.paymentDate());
        entity.setAmount(payment.amount());
        entity.setPaymentMethod(payment.paymentMethod());
        entity.setReference(payment.reference());
        entity.setCreatedBy(payment.createdBy());
        entity.setCreatedAt(payment.createdAt());
        return entity;
    }
}