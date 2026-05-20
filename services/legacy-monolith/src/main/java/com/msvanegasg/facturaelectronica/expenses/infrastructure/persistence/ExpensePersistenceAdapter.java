package com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ExpenseTypeJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.PaymentMethodJpaEntity;
import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;
import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;
import com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.entity.ExpenseJpaEntity;
import com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.repository.ExpenseJpaRepository;

@Component
public class ExpensePersistenceAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository expenseRepository;

    public ExpensePersistenceAdapter(ExpenseJpaRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense save(Expense expense) {
        return toDomain(expenseRepository.save(toEntity(expense)));
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id).map(ExpensePersistenceAdapter::toDomain);
    }

    @Override
    public List<Expense> findActive() {
        return expenseRepository.findByActivo(true).stream()
                .map(ExpensePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Expense> findByStatus(Estado status) {
        return expenseRepository.findByEstado(status).stream()
                .map(ExpensePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Expense> findByExpenseType(Long expenseTypeId) {
        return expenseRepository.findByTipoGastoIdTipoGasto(expenseTypeId).stream()
                .map(ExpensePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Expense> findByPaymentMethod(Long paymentMethodId) {
        return expenseRepository.findByMetodoPagoIdMetodoPago(paymentMethodId).stream()
                .map(ExpensePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Expense> findByDescriptionContainingIgnoreCase(String description) {
        return Optional.ofNullable(expenseRepository.findByDescripcionContainingIgnoreCase(description))
                .map(ExpensePersistenceAdapter::toDomain);
    }

    private static Expense toDomain(ExpenseJpaEntity entity) {
        return Expense.restore(
                entity.getIdGasto(),
                entity.getFecha(),
                entity.getMonto(),
                entity.getDescripcion(),
                toExpenseTypeSummary(entity.getTipoGasto()),
                toPaymentMethodSummary(entity.getMetodoPago()),
                entity.getUrlEvidencia(),
                entity.getEstado(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static ExpenseJpaEntity toEntity(Expense expense) {
        return ExpenseJpaEntity.builder()
                .idGasto(expense.id())
                .fecha(expense.date())
                .monto(expense.amount())
                .descripcion(expense.description())
                .tipoGasto(ExpenseTypeJpaEntity.builder()
                        .idTipoGasto(expense.expenseType().id())
                        .nombre(expense.expenseType().name())
                        .descripcion(expense.expenseType().description())
                        .activo(true)
                        .build())
                .metodoPago(PaymentMethodJpaEntity.builder()
                        .idMetodoPago(expense.paymentMethod().id())
                        .nombre(expense.paymentMethod().name())
                        .descripcion(expense.paymentMethod().description())
                        .activo(true)
                        .build())
                .urlEvidencia(expense.evidenceUrl())
                .estado(expense.status())
                .activo(expense.active())
                .build();
    }

    private static ExpenseTypeSummary toExpenseTypeSummary(ExpenseTypeJpaEntity entity) {
        return new ExpenseTypeSummary(entity.getIdTipoGasto(), entity.getNombre(), entity.getDescripcion());
    }

    private static PaymentMethodSummary toPaymentMethodSummary(PaymentMethodJpaEntity entity) {
        return new PaymentMethodSummary(entity.getIdMetodoPago(), entity.getNombre(), entity.getDescripcion());
    }
}
