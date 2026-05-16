package com.msvanegasg.facturaelectronica.expenses.application.port.out;

import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;

public interface ExpenseCatalogPort {

    ExpenseTypeSummary findExpenseType(Long id);

    PaymentMethodSummary findPaymentMethod(Long id);
}
