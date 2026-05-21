package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

@ExtendWith(MockitoExtension.class)
class AccountingControllerOperationsTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID EXPENSE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID PAYABLE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PAYMENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountingController(chartOfAccountsUseCase,
                accountingRulesUseCase, accountingEntryUseCase, accountingBooksUseCase, expenseUseCase,
                accountsPayableUseCase)).build();
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

    private static ExpenseResult expense(ExpenseStatus status) {
        return new ExpenseResult(EXPENSE_ID, COMPANY_ID, null, LocalDate.of(2026, 5, 20),
                "Servicio publico energia", money("100000.00"), money("19000.00"), money("119000.00"),
                PaymentCondition.CREDIT, LocalDate.of(2026, 6, 20), null, status, "expense-1", NOW,
                status == ExpenseStatus.CONFIRMED ? NOW : null);
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
