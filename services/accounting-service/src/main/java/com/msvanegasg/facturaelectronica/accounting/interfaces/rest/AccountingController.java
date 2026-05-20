package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerBookResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AccountingController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final ManageChartOfAccountsUseCase manageChartOfAccountsUseCase;
    private final ManageAccountingRulesUseCase manageAccountingRulesUseCase;
    private final GenerateAccountingEntryUseCase generateAccountingEntryUseCase;
    private final QueryAccountingBooksUseCase queryAccountingBooksUseCase;

    public AccountingController(
            ManageChartOfAccountsUseCase manageChartOfAccountsUseCase,
            ManageAccountingRulesUseCase manageAccountingRulesUseCase,
            GenerateAccountingEntryUseCase generateAccountingEntryUseCase,
            QueryAccountingBooksUseCase queryAccountingBooksUseCase) {
        this.manageChartOfAccountsUseCase = manageChartOfAccountsUseCase;
        this.manageAccountingRulesUseCase = manageAccountingRulesUseCase;
        this.generateAccountingEntryUseCase = generateAccountingEntryUseCase;
        this.queryAccountingBooksUseCase = queryAccountingBooksUseCase;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        manageChartOfAccountsUseCase.create(AccountingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/accounts")
    public ResponseEntity<AccountResponse> findAccountByCode(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam String code) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                manageChartOfAccountsUseCase.findByCode(companyId, code)));
    }

    @PostMapping("/accounting-rules")
    public ResponseEntity<AccountingRuleResponse> createAccountingRule(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountingRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        manageAccountingRulesUseCase.create(AccountingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/accounting-entries")
    public ResponseEntity<AccountingEntryResponse> generateAccountingEntry(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountingEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        generateAccountingEntryUseCase.generate(AccountingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/reports/journal")
    public ResponseEntity<JournalBookResponse> journalBook(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                queryAccountingBooksUseCase.journalBook(new JournalBookQuery(companyId, from, to))));
    }

    @GetMapping("/reports/ledger")
    public ResponseEntity<LedgerBookResponse> ledgerBook(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String accountCode) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                queryAccountingBooksUseCase.ledgerBook(new LedgerBookQuery(companyId, from, to)),
                accountCode));
    }
}
