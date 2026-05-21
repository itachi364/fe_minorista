package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateExpenseCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;

public interface ManageExpenseUseCase {

    ExpenseResult create(CreateExpenseCommand command);

    ExpenseResult confirm(UUID companyId, UUID expenseId);
}
