package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayablePayment;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsPayablePaymentJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountsPayablePaymentJpaRepository;

@Component
public class AccountsPayablePaymentPersistenceAdapter implements AccountsPayablePaymentRepositoryPort {

    private final AccountsPayablePaymentJpaRepository repository;

    public AccountsPayablePaymentPersistenceAdapter(AccountsPayablePaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountsPayablePayment save(AccountsPayablePayment payment) {
        return toDomain(repository.save(toEntity(payment)));
    }

    private static AccountsPayablePayment toDomain(AccountsPayablePaymentJpaEntity entity) {
        return new AccountsPayablePayment(entity.getId(), entity.getCompanyId(), entity.getAccountsPayableId(),
                entity.getPaymentDate(), entity.getAmount(), entity.getPaymentMethod(), entity.getReference(),
                entity.getCreatedBy(), entity.getCreatedAt());
    }

    private static AccountsPayablePaymentJpaEntity toEntity(AccountsPayablePayment payment) {
        AccountsPayablePaymentJpaEntity entity = new AccountsPayablePaymentJpaEntity();
        entity.setId(payment.id());
        entity.setCompanyId(payment.companyId());
        entity.setAccountsPayableId(payment.accountsPayableId());
        entity.setPaymentDate(payment.paymentDate());
        entity.setAmount(payment.amount());
        entity.setPaymentMethod(payment.paymentMethod());
        entity.setReference(payment.reference());
        entity.setCreatedBy(payment.createdBy());
        entity.setCreatedAt(payment.createdAt());
        return entity;
    }
}
