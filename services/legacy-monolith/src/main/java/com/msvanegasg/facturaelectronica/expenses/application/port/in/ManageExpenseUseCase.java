package com.msvanegasg.facturaelectronica.expenses.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.application.dto.ExpenseCommand;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;

public interface ManageExpenseUseCase {

    Expense create(ExpenseCommand command);

    Expense update(Long id, ExpenseCommand command);

    void disable(Long id);

    Expense findById(Long id);

    List<Expense> findActive();

    List<Expense> findByStatus(Estado status);

    List<Expense> findByExpenseType(Long expenseTypeId);

    List<Expense> findByPaymentMethod(Long paymentMethodId);

    Expense findByDescription(String description);
}
