package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.InitializeBasicAccountingSetupUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsReceivableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayablePaymentResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivablePaymentResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivableRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsReceivableResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayableRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountsPayableResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingEntryResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingRuleResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.AccountingSetupResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ExpenseRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ExpenseResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.JournalBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.LedgerBookResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.PayablePaymentRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.ReceivablePaymentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AccountingController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final InitializeBasicAccountingSetupUseCase initializeBasicAccountingSetupUseCase;
    private final ManageChartOfAccountsUseCase manageChartOfAccountsUseCase;
    private final ManageAccountingRulesUseCase manageAccountingRulesUseCase;
    private final GenerateAccountingEntryUseCase generateAccountingEntryUseCase;
    private final QueryAccountingBooksUseCase queryAccountingBooksUseCase;
    private final ManageExpenseUseCase manageExpenseUseCase;
    private final ManageAccountsPayableUseCase manageAccountsPayableUseCase;
    private final ManageAccountsReceivableUseCase manageAccountsReceivableUseCase;

    public AccountingController(
            InitializeBasicAccountingSetupUseCase initializeBasicAccountingSetupUseCase,
            ManageChartOfAccountsUseCase manageChartOfAccountsUseCase,
            ManageAccountingRulesUseCase manageAccountingRulesUseCase,
            GenerateAccountingEntryUseCase generateAccountingEntryUseCase,
            QueryAccountingBooksUseCase queryAccountingBooksUseCase,
            ManageExpenseUseCase manageExpenseUseCase,
            ManageAccountsPayableUseCase manageAccountsPayableUseCase,
            ManageAccountsReceivableUseCase manageAccountsReceivableUseCase) {
        this.initializeBasicAccountingSetupUseCase = initializeBasicAccountingSetupUseCase;
        this.manageChartOfAccountsUseCase = manageChartOfAccountsUseCase;
        this.manageAccountingRulesUseCase = manageAccountingRulesUseCase;
        this.generateAccountingEntryUseCase = generateAccountingEntryUseCase;
        this.queryAccountingBooksUseCase = queryAccountingBooksUseCase;
        this.manageExpenseUseCase = manageExpenseUseCase;
        this.manageAccountsPayableUseCase = manageAccountsPayableUseCase;
        this.manageAccountsReceivableUseCase = manageAccountsReceivableUseCase;
    }

    @PostMapping("/accounting-setup/basic")
    public ResponseEntity<AccountingSetupResponse> initializeBasicAccountingSetup(
            @RequestHeader(COMPANY_HEADER) UUID companyId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(initializeBasicAccountingSetupUseCase.initialize(companyId)));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        manageChartOfAccountsUseCase.create(AccountingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping(value = "/accounts", params = "code")
    public ResponseEntity<AccountResponse> findAccountByCode(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam String code) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                manageChartOfAccountsUseCase.findByCode(companyId, code)));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> accounts(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(manageChartOfAccountsUseCase.find(companyId, active).stream()
                .map(AccountingRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/accounting-rules")
    public ResponseEntity<AccountingRuleResponse> createAccountingRule(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountingRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        manageAccountingRulesUseCase.create(AccountingRestMapper.toCommand(companyId, request))));
    }

    @PutMapping("/accounting-rules/active")
    public ResponseEntity<AccountingRuleResponse> replaceActiveAccountingRule(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountingRuleRequest request) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                manageAccountingRulesUseCase.replaceActive(AccountingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/accounting-rules/{eventType}/deactivate")
    public ResponseEntity<AccountingRuleResponse> deactivateActiveAccountingRule(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable AccountingEventType eventType) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                manageAccountingRulesUseCase.deactivateActive(companyId, eventType)));
    }

    @GetMapping("/accounting-rules")
    public ResponseEntity<List<AccountingRuleResponse>> accountingRules(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) AccountingEventType eventType,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(manageAccountingRulesUseCase.find(companyId, eventType, active).stream()
                .map(AccountingRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/accounting-entries")
    public ResponseEntity<AccountingEntryResponse> generateAccountingEntry(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountingEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(
                        generateAccountingEntryUseCase.generate(AccountingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(manageExpenseUseCase.create(
                        AccountingRestMapper.toCommand(companyId, request, idempotencyKey))));
    }

    @PostMapping("/expenses/{expenseId}/confirm")
    public ResponseEntity<ExpenseResponse> confirmExpense(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID expenseId) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(manageExpenseUseCase.confirm(companyId, expenseId)));
    }

    @GetMapping("/reports/expenses")
    public ResponseEntity<List<ExpenseResponse>> expenseReport(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(manageExpenseUseCase.find(new ExpenseQuery(companyId, status, supplierId, from, to))
                .stream().map(AccountingRestMapper::toResponse).toList());
    }

    @GetMapping("/accounts-payable")
    public ResponseEntity<List<AccountsPayableResponse>> accountsPayable(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) AccountsPayableStatus status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(manageAccountsPayableUseCase.find(companyId, status, supplierId, from, to).stream()
                .map(AccountingRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/accounts-payable")
    public ResponseEntity<AccountsPayableResponse> createAccountsPayable(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountsPayableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(manageAccountsPayableUseCase.create(
                        AccountingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/accounts-payable/{payableId}/payments")
    public ResponseEntity<AccountsPayablePaymentResponse> registerPayablePayment(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID payableId,
            @Valid @RequestBody PayablePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(manageAccountsPayableUseCase.registerPayment(
                        AccountingRestMapper.toCommand(companyId, payableId, request, userId))));
    }


    @GetMapping("/accounts-receivable")
    public ResponseEntity<List<AccountsReceivableResponse>> accountsReceivable(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) AccountsReceivableStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(manageAccountsReceivableUseCase.find(companyId, status, customerId, from, to).stream()
                .map(AccountingRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/reports/accounts-receivable")
    public ResponseEntity<List<AccountsReceivableResponse>> accountsReceivableReport(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) AccountsReceivableStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return accountsReceivable(companyId, status, customerId, from, to);
    }

    @PostMapping("/accounts-receivable")
    public ResponseEntity<AccountsReceivableResponse> createAccountsReceivable(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AccountsReceivableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(manageAccountsReceivableUseCase.create(
                        AccountingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/accounts-receivable/{receivableId}/payments")
    public ResponseEntity<AccountsReceivablePaymentResponse> registerReceivablePayment(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID receivableId,
            @Valid @RequestBody ReceivablePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountingRestMapper.toResponse(manageAccountsReceivableUseCase.registerPayment(
                        AccountingRestMapper.toCommand(companyId, receivableId, request, userId))));
    }
    @GetMapping("/reports/journal")
    public ResponseEntity<JournalBookResponse> journalBook(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                queryAccountingBooksUseCase.journalBook(new JournalBookQuery(companyId, from, to))));
    }

    @GetMapping("/reports/trial-balance")
    public ResponseEntity<LedgerBookResponse> trialBalance(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String accountCode) {
        return ResponseEntity.ok(AccountingRestMapper.toResponse(
                queryAccountingBooksUseCase.ledgerBook(new LedgerBookQuery(companyId, from, to)),
                accountCode));
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