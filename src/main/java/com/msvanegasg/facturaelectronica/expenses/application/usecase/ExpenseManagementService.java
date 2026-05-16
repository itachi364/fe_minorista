package com.msvanegasg.facturaelectronica.expenses.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoInactivoException;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoNotFoundException;
import com.msvanegasg.facturaelectronica.expenses.application.dto.ExpenseCommand;
import com.msvanegasg.facturaelectronica.expenses.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseCatalogPort;
import com.msvanegasg.facturaelectronica.expenses.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;
import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;

public class ExpenseManagementService implements ManageExpenseUseCase {

    private final ExpenseRepositoryPort expenseRepository;
    private final ExpenseCatalogPort expenseCatalog;

    public ExpenseManagementService(ExpenseRepositoryPort expenseRepository, ExpenseCatalogPort expenseCatalog) {
        this.expenseRepository = Objects.requireNonNull(expenseRepository);
        this.expenseCatalog = Objects.requireNonNull(expenseCatalog);
    }

    @Override
    public Expense create(ExpenseCommand command) {
        Objects.requireNonNull(command, "command is required");
        ExpenseTypeSummary expenseType = expenseCatalog.findExpenseType(command.expenseTypeId());
        PaymentMethodSummary paymentMethod = expenseCatalog.findPaymentMethod(command.paymentMethodId());
        return expenseRepository.save(Expense.create(command.date(), command.amount(), command.description(),
                expenseType, paymentMethod, command.evidenceUrl(), command.status()));
    }

    @Override
    public Expense update(Long id, ExpenseCommand command) {
        Objects.requireNonNull(command, "command is required");
        Expense existing = findById(id);
        if (!existing.active()) {
            throw new GastoInactivoException(id);
        }
        ExpenseTypeSummary expenseType = expenseCatalog.findExpenseType(command.expenseTypeId());
        PaymentMethodSummary paymentMethod = expenseCatalog.findPaymentMethod(command.paymentMethodId());
        return expenseRepository.save(existing.update(command.date(), command.amount(), command.description(),
                expenseType, paymentMethod, command.evidenceUrl(), command.status()));
    }

    @Override
    public void disable(Long id) {
        Expense expense = findById(id);
        expenseRepository.save(expense.disable());
    }

    @Override
    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new GastoNotFoundException(id));
    }

    @Override
    public List<Expense> findActive() {
        return expenseRepository.findActive();
    }

    @Override
    public List<Expense> findByStatus(Estado status) {
        return expenseRepository.findByStatus(status);
    }

    @Override
    public List<Expense> findByExpenseType(Long expenseTypeId) {
        return expenseRepository.findByExpenseType(expenseTypeId);
    }

    @Override
    public List<Expense> findByPaymentMethod(Long paymentMethodId) {
        return expenseRepository.findByPaymentMethod(paymentMethodId);
    }

    @Override
    public Expense findByDescription(String description) {
        return expenseRepository.findByDescriptionContainingIgnoreCase(description)
                .orElseThrow(() -> new GastoNotFoundException((Long) null));
    }
}
