package com.msvanegasg.facturaelectronica.expenses.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;

public interface ExpenseRepositoryPort {

    Expense save(Expense expense);

    Optional<Expense> findById(Long id);

    List<Expense> findActive();

    List<Expense> findByStatus(Estado status);

    List<Expense> findByExpenseType(Long expenseTypeId);

    List<Expense> findByPaymentMethod(Long paymentMethodId);

    Optional<Expense> findByDescriptionContainingIgnoreCase(String description);
}
