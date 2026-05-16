package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleLineRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookLineResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerAccountSummaryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerBookResponse;

public final class AccountingRestMapper {

    private AccountingRestMapper() {
    }

    public static CreateAccountCommand toCommand(UUID companyId, AccountRequest request) {
        return new CreateAccountCommand(companyId, request.code(), request.name(), request.parentAccountId());
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

    private static CreateAccountingRuleLineCommand toCommand(AccountingRuleLineRequest request) {
        return new CreateAccountingRuleLineCommand(
                request.accountCode(),
                request.side(),
                request.amountType(),
                request.description());
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
}
