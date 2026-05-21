package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayablePayment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;

final class AccountingOperationsMapper {

    private AccountingOperationsMapper() {
    }

    static ExpenseResult toResult(Expense expense) {
        return new ExpenseResult(expense.id(), expense.companyId(), expense.supplierId(), expense.expenseDate(),
                expense.concept(), expense.subtotal(), expense.taxTotal(), expense.total(), expense.paymentCondition(),
                expense.dueDate(), expense.evidenceUrl(), expense.status(), expense.idempotencyKey(),
                expense.createdAt(), expense.confirmedAt());
    }

    static AccountsPayableResult toResult(AccountsPayable payable) {
        return new AccountsPayableResult(payable.id(), payable.companyId(), payable.supplierId(),
                payable.sourceType(), payable.sourceId(), payable.issueDate(), payable.dueDate(),
                payable.totalAmount(), payable.paidAmount(), payable.balance(), payable.status(), payable.createdAt());
    }

    static AccountsPayablePaymentResult toResult(AccountsPayablePayment payment, AccountsPayable payable) {
        return new AccountsPayablePaymentResult(payment.id(), payment.companyId(), payment.accountsPayableId(),
                payment.paymentDate(), payment.amount(), payment.paymentMethod(), payment.reference(),
                payment.createdBy(), payment.createdAt(), toResult(payable));
    }
}
