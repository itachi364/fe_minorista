package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingConfigurationCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsPayableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsReceivableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateExpenseCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FinancialStatementGroupResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FinancialStatementResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterPayablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterReceivablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayablePaymentResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivablePaymentResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivableRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivableResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayableRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayableResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingConfigurationRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleLineRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingSetupResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ExpenseRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ExpenseResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FinancialStatementGroupResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FinancialStatementResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerAccountSummaryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.PayablePaymentRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ReceivablePaymentRequest;

public final class AccountingRestMapper {

    private AccountingRestMapper() {
    }

    public static CreateAccountCommand toCommand(UUID companyId, AccountRequest request) {
        return new CreateAccountCommand(companyId, request.code(), request.name(), request.parentAccountId());
    }

    public static AccountingConfigurationCommand toCommand(UUID companyId, AccountingConfigurationRequest request) {
        return new AccountingConfigurationCommand(
                companyId,
                safeAccounts(request).stream().map(account -> toCommand(companyId, account)).toList(),
                safeRules(request).stream().map(rule -> toCommand(companyId, rule)).toList());
    }

    public static CreateAccountingRuleCommand toCommand(UUID companyId, AccountingRuleRequest request) {
        return new CreateAccountingRuleCommand(
                companyId,
                request.eventType(),
                request.sourceType(),
                request.name(),
                request.lines().stream().map(AccountingRestMapper::toCommand).toList());
    }

    public static GenerateAccountingEntryCommand toCommand(UUID companyId, AccountingEntryRequest request) {
        return new GenerateAccountingEntryCommand(
                companyId,
                request.eventType(),
                request.sourceType(),
                request.sourceId(),
                request.entryDate(),
                request.description(),
                request.thirdpartyId(),
                request.subtotal(),
                request.taxTotal(),
                request.total());
    }

    public static CreateExpenseCommand toCommand(UUID companyId, ExpenseRequest request, String idempotencyKey) {
        return new CreateExpenseCommand(companyId, request.supplierId(), request.expenseDate(), request.concept(),
                request.subtotal(), request.taxTotal(), request.total(), request.paymentCondition(),
                request.dueDate(), request.evidenceUrl(), idempotencyKey);
    }

    public static RegisterPayablePaymentCommand toCommand(UUID companyId, UUID payableId,
            PayablePaymentRequest request, UUID createdBy) {
        return new RegisterPayablePaymentCommand(companyId, payableId, request.paymentDate(), request.amount(),
                request.paymentMethod(), request.reference(), createdBy);
    }


    public static RegisterReceivablePaymentCommand toCommand(UUID companyId, UUID receivableId,
            ReceivablePaymentRequest request, UUID createdBy) {
        return new RegisterReceivablePaymentCommand(companyId, receivableId, request.paymentDate(), request.amount(),
                request.paymentMethod(), request.reference(), createdBy);
    }

    public static CreateAccountsReceivableCommand toCommand(UUID companyId, AccountsReceivableRequest request) {
        return new CreateAccountsReceivableCommand(companyId, request.customerId(), request.sourceType(),
                request.sourceId(), request.issueDate(), request.dueDate(), request.totalAmount(),
                request.idempotencyKey());
    }
    public static CreateAccountsPayableCommand toCommand(UUID companyId, AccountsPayableRequest request) {
        return new CreateAccountsPayableCommand(companyId, request.supplierId(), request.sourceType(),
                request.sourceId(), request.issueDate(), request.dueDate(), request.totalAmount());
    }

    public static AccountResponse toResponse(AccountResult result) {
        return new AccountResponse(
                result.id(),
                result.companyId(),
                result.code(),
                result.name(),
                result.category(),
                result.level(),
                result.nature(),
                result.parentAccountId(),
                result.active());
    }

    public static AccountingRuleResponse toResponse(AccountingRuleResult result) {
        return new AccountingRuleResponse(
                result.id(),
                result.companyId(),
                result.eventType(),
                result.sourceType(),
                result.name(),
                result.lines().stream().map(AccountingRestMapper::toResponse).toList(),
                result.active());
    }


    public static AccountingSetupResponse toResponse(AccountingSetupResult result) {
        return new AccountingSetupResponse(
                result.companyId(),
                result.templateName(),
                result.accounts().stream().map(AccountingRestMapper::toResponse).toList(),
                result.rules().stream().map(AccountingRestMapper::toResponse).toList());
    }
    public static AccountingEntryResponse toResponse(AccountingEntryResult result) {
        return new AccountingEntryResponse(
                result.id(),
                result.companyId(),
                result.entryDate(),
                result.description(),
                result.sourceType(),
                result.sourceId(),
                result.status(),
                result.debitTotal(),
                result.creditTotal(),
                result.lines().stream().map(AccountingRestMapper::toResponse).toList());
    }

    public static ExpenseResponse toResponse(ExpenseResult result) {
        return new ExpenseResponse(result.id(), result.companyId(), result.supplierId(), result.expenseDate(),
                result.concept(), result.subtotal(), result.taxTotal(), result.total(), result.paymentCondition(),
                result.dueDate(), result.evidenceUrl(), result.status(), result.idempotencyKey(), result.createdAt(),
                result.confirmedAt());
    }


    public static AccountsReceivableResponse toResponse(AccountsReceivableResult result) {
        return new AccountsReceivableResponse(result.id(), result.companyId(), result.customerId(), result.sourceType(),
                result.sourceId(), result.issueDate(), result.dueDate(), result.totalAmount(), result.paidAmount(),
                result.balance(), result.status(), result.idempotencyKey(), result.createdAt());
    }

    public static AccountsReceivablePaymentResponse toResponse(AccountsReceivablePaymentResult result) {
        return new AccountsReceivablePaymentResponse(result.id(), result.companyId(), result.accountsReceivableId(),
                result.paymentDate(), result.amount(), result.paymentMethod(), result.reference(), result.createdBy(),
                result.createdAt(), toResponse(result.receivable()));
    }
    public static AccountsPayableResponse toResponse(AccountsPayableResult result) {
        return new AccountsPayableResponse(result.id(), result.companyId(), result.supplierId(), result.sourceType(),
                result.sourceId(), result.issueDate(), result.dueDate(), result.totalAmount(), result.paidAmount(),
                result.balance(), result.status(), result.createdAt());
    }

    public static AccountsPayablePaymentResponse toResponse(AccountsPayablePaymentResult result) {
        return new AccountsPayablePaymentResponse(result.id(), result.companyId(), result.accountsPayableId(),
                result.paymentDate(), result.amount(), result.paymentMethod(), result.reference(), result.createdBy(),
                result.createdAt(), toResponse(result.payable()));
    }

    public static JournalBookResponse toResponse(JournalBookResult result) {
        return new JournalBookResponse(
                result.companyId(),
                result.fromDate(),
                result.toDate(),
                result.debitTotal(),
                result.creditTotal(),
                result.entries().stream().map(AccountingRestMapper::toResponse).toList());
    }

    public static LedgerBookResponse toResponse(LedgerBookResult result, String accountCode) {
        List<LedgerAccountSummaryResult> accounts = result.accounts();
        if (accountCode != null && !accountCode.isBlank()) {
            accounts = accounts.stream()
                    .filter(account -> account.accountCode().equals(accountCode.trim()))
                    .toList();
        }
        return new LedgerBookResponse(
                result.companyId(),
                result.fromDate(),
                result.toDate(),
                result.debitTotal(),
                result.creditTotal(),
                accounts.stream().map(AccountingRestMapper::toResponse).toList());
    }

    public static FinancialStatementResponse toResponse(FinancialStatementResult result) {
        return new FinancialStatementResponse(
                result.companyId(),
                result.fromDate(),
                result.toDate(),
                result.statementType(),
                result.groups().stream().map(AccountingRestMapper::toResponse).toList(),
                result.total());
    }

    private static CreateAccountingRuleLineCommand toCommand(AccountingRuleLineRequest request) {
        return new CreateAccountingRuleLineCommand(
                request.accountCode(),
                request.side(),
                request.amountType(),
                request.description());
    }

    private static List<AccountRequest> safeAccounts(AccountingConfigurationRequest request) {
        return request.accounts() == null ? List.of() : request.accounts();
    }

    private static List<AccountingRuleRequest> safeRules(AccountingConfigurationRequest request) {
        return request.rules() == null ? List.of() : request.rules();
    }

    private static AccountingRuleLineResponse toResponse(AccountingRuleLineResult result) {
        return new AccountingRuleLineResponse(
                result.accountCode(),
                result.side(),
                result.amountType(),
                result.description());
    }

    private static AccountingEntryLineResponse toResponse(AccountingEntryLineResult result) {
        return new AccountingEntryLineResponse(
                result.id(),
                result.accountId(),
                result.accountCode(),
                result.accountName(),
                result.thirdpartyId(),
                result.debitAmount(),
                result.creditAmount(),
                result.description());
    }

    private static JournalBookEntryResponse toResponse(JournalBookEntryResult result) {
        return new JournalBookEntryResponse(
                result.entryId(),
                result.entryDate(),
                result.description(),
                result.sourceType(),
                result.sourceId(),
                result.debitTotal(),
                result.creditTotal(),
                result.lines().stream().map(AccountingRestMapper::toResponse).toList());
    }

    private static JournalBookLineResponse toResponse(JournalBookLineResult result) {
        return new JournalBookLineResponse(
                result.lineId(),
                result.accountId(),
                result.accountCode(),
                result.accountName(),
                result.thirdpartyId(),
                result.debitAmount(),
                result.creditAmount(),
                result.description());
    }

    private static LedgerAccountSummaryResponse toResponse(LedgerAccountSummaryResult result) {
        return new LedgerAccountSummaryResponse(
                result.accountId(),
                result.accountCode(),
                result.accountName(),
                result.nature(),
                result.debitTotal(),
                result.creditTotal(),
                result.balance());
    }

    private static FinancialStatementGroupResponse toResponse(FinancialStatementGroupResult result) {
        return new FinancialStatementGroupResponse(result.code(), result.label(), result.total());
    }
}
