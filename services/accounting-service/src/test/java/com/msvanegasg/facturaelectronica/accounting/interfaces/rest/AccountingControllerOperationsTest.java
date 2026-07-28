package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.InitializeBasicAccountingSetupUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsReceivableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountLevel;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

@ExtendWith(MockitoExtension.class)
class AccountingControllerOperationsTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID EXPENSE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID PAYABLE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PAYMENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID RECEIVABLE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private InitializeBasicAccountingSetupUseCase setupUseCase;
    @Mock
    private ManageChartOfAccountsUseCase chartOfAccountsUseCase;
    @Mock
    private ManageAccountingRulesUseCase accountingRulesUseCase;
    @Mock
    private GenerateAccountingEntryUseCase accountingEntryUseCase;
    @Mock
    private QueryAccountingBooksUseCase accountingBooksUseCase;
    @Mock
    private ManageExpenseUseCase expenseUseCase;
    @Mock
    private ManageAccountsPayableUseCase accountsPayableUseCase;
    @Mock
    private ManageAccountsReceivableUseCase accountsReceivableUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountingController(setupUseCase, chartOfAccountsUseCase,
                accountingRulesUseCase, accountingEntryUseCase, accountingBooksUseCase, expenseUseCase,
                accountsPayableUseCase, accountsReceivableUseCase)).build();
    }

    @Test
    void initializesBasicAccountingSetup() throws Exception {
        when(setupUseCase.initialize(COMPANY_ID)).thenReturn(new AccountingSetupResult(COMPANY_ID,
                "BASIC_COLOMBIA_SMALL_BUSINESS", List.of(account("1105")), List.of(rule("Venta basica", true))));

        mockMvc.perform(post("/api/v1/accounting-setup/basic")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateName").value("BASIC_COLOMBIA_SMALL_BUSINESS"))
                .andExpect(jsonPath("$.accounts[0].code").value("1105"))
                .andExpect(jsonPath("$.rules[0].eventType").value("SALE_CONFIRMED"));
    }
    @Test
    void listsAccountsAndReplacesAccountingRules() throws Exception {
        when(chartOfAccountsUseCase.find(COMPANY_ID, true)).thenReturn(List.of(account("1105")));
        when(accountingRulesUseCase.replaceActive(any())).thenReturn(rule("Venta reemplazada", true));
        when(accountingRulesUseCase.find(any(), any(), any())).thenReturn(List.of(rule("Venta reemplazada", true)));

        mockMvc.perform(get("/api/v1/accounts")
                .header("X-Company-Id", COMPANY_ID)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("1105"));

        mockMvc.perform(put("/api/v1/accounting-rules/active")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "eventType": "SALE_CONFIRMED",
                          "sourceType": "SALE",
                          "name": "Venta reemplazada",
                          "lines": [
                            {"accountCode": "1105", "side": "DEBIT", "amountType": "TOTAL"},
                            {"accountCode": "4135", "side": "CREDIT", "amountType": "SUBTOTAL"},
                            {"accountCode": "2408", "side": "CREDIT", "amountType": "TAX_TOTAL"}
                          ]
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Venta reemplazada"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/accounting-rules")
                .header("X-Company-Id", COMPANY_ID)
                .param("eventType", "SALE_CONFIRMED")
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("SALE_CONFIRMED"));
    }

    @Test
    void reportsExpenses() throws Exception {
        when(expenseUseCase.find(any(ExpenseQuery.class))).thenReturn(List.of(expense(ExpenseStatus.CONFIRMED)));

        mockMvc.perform(get("/api/v1/reports/expenses")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "CONFIRMED")
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].total").value(119000));
    }

    @Test
    void createsAndConfirmsExpense() throws Exception {
        when(expenseUseCase.create(any())).thenReturn(expense(ExpenseStatus.PENDING));
        when(expenseUseCase.confirm(COMPANY_ID, EXPENSE_ID)).thenReturn(expense(ExpenseStatus.CONFIRMED));

        mockMvc.perform(post("/api/v1/expenses")
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "expense-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "expenseDate": "2026-05-20",
                          "concept": "Servicio publico energia",
                          "subtotal": 100000.00,
                          "taxTotal": 19000.00,
                          "total": 119000.00,
                          "paymentCondition": "CREDIT",
                          "dueDate": "2026-06-20"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/expenses/{expenseId}/confirm", EXPENSE_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }


    @Test
    void reportsTrialBalance() throws Exception {
        when(accountingBooksUseCase.ledgerBook(any())).thenReturn(new LedgerBookResult(COMPANY_ID,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), money("35700.00"), money("35700.00"),
                List.of(new LedgerAccountSummaryResult(UUID.randomUUID(), "1105", "Caja", AccountNature.DEBIT,
                        money("35700.00"), BigDecimal.ZERO, money("35700.00")))));

        mockMvc.perform(get("/api/v1/reports/trial-balance")
                .header("X-Company-Id", COMPANY_ID)
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountCode").value("1105"))
                .andExpect(jsonPath("$.debitTotal").value(35700));
    }

    @Test
    void queriesPayablesAndRegistersPayment() throws Exception {
        when(accountsPayableUseCase.find(any(), any(), any(), any(), any())).thenReturn(List.of(payable()));
        when(accountsPayableUseCase.registerPayment(any())).thenReturn(payment());

        mockMvc.perform(get("/api/v1/accounts-payable")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(119000));

        mockMvc.perform(post("/api/v1/accounts-payable/{payableId}/payments", PAYABLE_ID)
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "paymentDate": "2026-05-25",
                          "amount": 40000.00,
                          "paymentMethod": "BANK_TRANSFER",
                          "reference": "TRX-1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(40000));
    }


    @Test
    void createsQueriesReportsReceivablesAndRegistersPayment() throws Exception {
        when(accountsReceivableUseCase.create(any())).thenReturn(receivable());
        when(accountsReceivableUseCase.find(any(), any(), any(), any(), any())).thenReturn(List.of(receivable()));
        when(accountsReceivableUseCase.registerPayment(any())).thenReturn(receivablePayment());

        mockMvc.perform(post("/api/v1/accounts-receivable")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "customerId": "11111111-2222-3333-4444-555555555555",
                          "sourceType": "SALE",
                          "sourceId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                          "issueDate": "2026-05-20",
                          "dueDate": "2026-06-20",
                          "totalAmount": 119000.00,
                          "idempotencyKey": "sale-credit-1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/v1/accounts-receivable")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(119000));

        mockMvc.perform(get("/api/v1/reports/accounts-receivable")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idempotencyKey").value("sale-credit-1"));

        mockMvc.perform(post("/api/v1/accounts-receivable/{receivableId}/payments", RECEIVABLE_ID)
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "paymentDate": "2026-05-25",
                          "amount": 40000.00,
                          "paymentMethod": "BANK_TRANSFER",
                          "reference": "RC-1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(40000));
    }
    private static AccountResult account(String code) {
        return new AccountResult(UUID.randomUUID(), COMPANY_ID, code, "Cuenta " + code, AccountCategory.ASSET,
                AccountLevel.ACCOUNT, AccountNature.DEBIT, null, true);
    }

    private static AccountingRuleResult rule(String name, boolean active) {
        return new AccountingRuleResult(UUID.randomUUID(), COMPANY_ID, AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE, name, List.of(
                        new AccountingRuleLineResult("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                "Caja"),
                        new AccountingRuleLineResult("4135", AccountingEntrySide.CREDIT,
                                AccountingAmountType.SUBTOTAL, "Ingresos"),
                        new AccountingRuleLineResult("2408", AccountingEntrySide.CREDIT,
                                AccountingAmountType.TAX_TOTAL, "IVA")), active);
    }

    private static ExpenseResult expense(ExpenseStatus status) {
        return new ExpenseResult(EXPENSE_ID, COMPANY_ID, null, LocalDate.of(2026, 5, 20),
                "Servicio publico energia", money("100000.00"), money("19000.00"), money("119000.00"),
                PaymentCondition.CREDIT, LocalDate.of(2026, 6, 20), null, status, "expense-1", NOW,
                status == ExpenseStatus.CONFIRMED ? NOW : null);
    }


    private static AccountsReceivableResult receivable() {
        return new AccountsReceivableResult(RECEIVABLE_ID, COMPANY_ID,
                UUID.fromString("11111111-2222-3333-4444-555555555555"), AccountingSourceType.SALE, EXPENSE_ID,
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 20), money("119000.00"), BigDecimal.ZERO,
                money("119000.00"), AccountsReceivableStatus.OPEN, "sale-credit-1", NOW);
    }

    private static AccountsReceivablePaymentResult receivablePayment() {
        return new AccountsReceivablePaymentResult(PAYMENT_ID, COMPANY_ID, RECEIVABLE_ID, LocalDate.of(2026, 5, 25),
                money("40000.00"), "BANK_TRANSFER", "RC-1", null, NOW, receivable());
    }
    private static AccountsPayableResult payable() {
        return new AccountsPayableResult(PAYABLE_ID, COMPANY_ID, null, AccountingSourceType.EXPENSE, EXPENSE_ID,
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 20), money("119000.00"), BigDecimal.ZERO,
                money("119000.00"), AccountsPayableStatus.OPEN, NOW);
    }

    private static AccountsPayablePaymentResult payment() {
        return new AccountsPayablePaymentResult(PAYMENT_ID, COMPANY_ID, PAYABLE_ID, LocalDate.of(2026, 5, 25),
                money("40000.00"), "BANK_TRANSFER", "TRX-1", null, NOW, payable());
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
