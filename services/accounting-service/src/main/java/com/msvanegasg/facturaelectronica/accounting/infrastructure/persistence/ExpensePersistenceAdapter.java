package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.ExpenseJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.ExpenseJpaRepository;

@Component
public class ExpensePersistenceAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository repository;

    public ExpensePersistenceAdapter(ExpenseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Expense> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(ExpensePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Expense> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey) {
        return repository.findByCompanyIdAndIdempotencyKey(companyId, idempotencyKey)
                .map(ExpensePersistenceAdapter::toDomain);
    }

    @Override
    public Expense save(Expense expense) {
        return toDomain(repository.save(toEntity(expense)));
    }

    private static Expense toDomain(ExpenseJpaEntity entity) {
        return new Expense(entity.getId(), entity.getCompanyId(), entity.getSupplierId(), entity.getExpenseDate(),
                entity.getConcept(), entity.getSubtotal(), entity.getTaxTotal(), entity.getTotal(),
                entity.getPaymentCondition(), entity.getDueDate(), entity.getEvidenceUrl(), entity.getStatus(),
                entity.getIdempotencyKey(), entity.getCreatedAt(), entity.getConfirmedAt());
    }

    private static ExpenseJpaEntity toEntity(Expense expense) {
        ExpenseJpaEntity entity = new ExpenseJpaEntity();
        entity.setId(expense.id());
        entity.setCompanyId(expense.companyId());
        entity.setSupplierId(expense.supplierId());
        entity.setExpenseDate(expense.expenseDate());
        entity.setConcept(expense.concept());
        entity.setSubtotal(expense.subtotal());
        entity.setTaxTotal(expense.taxTotal());
        entity.setTotal(expense.total());
        entity.setPaymentCondition(expense.paymentCondition());
        entity.setDueDate(expense.dueDate());
        entity.setEvidenceUrl(expense.evidenceUrl());
        entity.setStatus(expense.status());
        entity.setIdempotencyKey(expense.idempotencyKey());
        entity.setCreatedAt(expense.createdAt());
        entity.setConfirmedAt(expense.confirmedAt());
        return entity;
    }
}
