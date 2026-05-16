package com.msvanegasg.facturaelectronica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountLevel;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.AccountingController;
import com.msvanegasg.facturaelectronica.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AccountingControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID ACCOUNT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ENTRY_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID LINE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID SOURCE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2026, 5, 15);

    private MockMvc mockMvc;

    @Mock
    private ManageChartOfAccountsUseCase manageChartOfAccountsUseCase;

    @Mock
    private ManageAccountingRulesUseCase manageAccountingRulesUseCase;

    @Mock
    private GenerateAccountingEntryUseCase generateAccountingEntryUseCase;

    @Mock
    private QueryAccountingBooksUseCase queryAccountingBooksUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountingController(
                        manageChartOfAccountsUseCase,
                        manageAccountingRulesUseCase,
                        generateAccountingEntryUseCase,
                        queryAccountingBooksUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAccountReturnsClassifiedPucAccount() throws Exception {
        when(manageChartOfAccountsUseCase.create(any())).thenReturn(accountResult());

        mockMvc.perform(post("/api/v1/accounts")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "code":"1105",
                          "name":"Caja"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.code").value("1105"))
                .andExpect(jsonPath("$.category").value("ASSET"))
                .andExpect(jsonPath("$.nature").value("DEBIT"));
    }

    @Test
    void findAccountByCodeReturnsAccount() throws Exception {
        when(manageChartOfAccountsUseCase.findByCode(eq(COMPANY_ID), eq("1105"))).thenReturn(accountResult());

        mockMvc.perform(get("/api/v1/accounts")
                .header("X-Company-Id", COMPANY_ID)
                .param("code", "1105"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.code").value("1105"));
    }

    @Test
    void createAccountingRuleReturnsConfiguredLines() throws Exception {
        when(manageAccountingRulesUseCase.create(any())).thenReturn(ruleResult());

        mockMvc.perform(post("/api/v1/accounting-rules")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "eventType":"SALE_CONFIRMED",
                          "sourceType":"SALE",
                          "name":"Venta POS",
                          "lines":[
                            {"accountCode":"1105","side":"DEBIT","amountType":"TOTAL"},
                            {"accountCode":"4135","side":"CREDIT","amountType":"SUBTOTAL"}
                          ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("SALE_CONFIRMED"))
                .andExpect(jsonPath("$.lines[0].accountCode").value("1105"))
                .andExpect(jsonPath("$.lines[0].side").value("DEBIT"));
    }

    @Test
    void generateAccountingEntryReturnsPostedEntry() throws Exception {
        when(generateAccountingEntryUseCase.generate(any())).thenReturn(entryResult());

        mockMvc.perform(post("/api/v1/accounting-entries")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "eventType":"SALE_CONFIRMED",
                          "sourceType":"SALE",
                          "sourceId":"dddddddd-dddd-dddd-dddd-dddddddddddd",
                          "entryDate":"2026-05-15",
                          "description":"Venta POS 1",
                          "subtotal":100.00,
                          "taxTotal":19.00,
                          "total":119.00
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.debitTotal").value(119.00))
                .andExpect(jsonPath("$.creditTotal").value(119.00));
    }

    @Test
    void journalBookReturnsEntriesForPeriod() throws Exception {
        when(queryAccountingBooksUseCase.journalBook(any())).thenReturn(journalResult());

        mockMvc.perform(get("/api/v1/reports/journal")
                .header("X-Company-Id", COMPANY_ID)
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].entryId").value(ENTRY_ID.toString()))
                .andExpect(jsonPath("$.entries[0].lines[0].accountCode").value("1105"));
    }

    @Test
    void ledgerBookFiltersByAccountCodeWhenProvided() throws Exception {
        when(queryAccountingBooksUseCase.ledgerBook(any())).thenReturn(ledgerResult());

        mockMvc.perform(get("/api/v1/reports/ledger")
                .header("X-Company-Id", COMPANY_ID)
                .param("from", "2026-05-01")
                .param("to", "2026-05-31")
                .param("accountCode", "1105"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()").value(1))
                .andExpect(jsonPath("$.accounts[0].accountCode").value("1105"));
    }

    private static AccountResult accountResult() {
        return new AccountResult(
                ACCOUNT_ID,
                COMPANY_ID,
                "1105",
                "Caja",
                AccountCategory.ASSET,
                AccountLevel.ACCOUNT,
                AccountNature.DEBIT,
                null,
                true);
    }

    private static AccountingRuleResult ruleResult() {
        return new AccountingRuleResult(
                UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta POS",
                List.of(
                        new AccountingRuleLineResult(
                                "1105",
                                AccountingEntrySide.DEBIT,
                                AccountingAmountType.TOTAL,
                                null),
                        new AccountingRuleLineResult(
                                "4135",
                                AccountingEntrySide.CREDIT,
                                AccountingAmountType.SUBTOTAL,
                                null)),
                true);
    }

    private static AccountingEntryResult entryResult() {
        return new AccountingEntryResult(
                ENTRY_ID,
                COMPANY_ID,
                ENTRY_DATE,
                "Venta POS 1",
                AccountingSourceType.SALE,
                SOURCE_ID,
                AccountingEntryStatus.POSTED,
                money("119.00"),
                money("119.00"),
                List.of(new AccountingEntryLineResult(
                        LINE_ID,
                        ACCOUNT_ID,
                        "1105",
                        "Caja",
                        null,
                        money("119.00"),
                        money("0.00"),
                        "Caja")));
    }

    private static JournalBookResult journalResult() {
        return new JournalBookResult(
                COMPANY_ID,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                money("119.00"),
                money("119.00"),
                List.of(new JournalBookEntryResult(
                        ENTRY_ID,
                        ENTRY_DATE,
                        "Venta POS 1",
                        AccountingSourceType.SALE,
                        SOURCE_ID,
                        money("119.00"),
                        money("119.00"),
                        List.of(new JournalBookLineResult(
                                LINE_ID,
                                ACCOUNT_ID,
                                "1105",
                                "Caja",
                                null,
                                money("119.00"),
                                money("0.00"),
                                "Caja")))));
    }

    private static LedgerBookResult ledgerResult() {
        return new LedgerBookResult(
                COMPANY_ID,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                money("119.00"),
                money("119.00"),
                List.of(
                        new LedgerAccountSummaryResult(
                                ACCOUNT_ID,
                                "1105",
                                "Caja",
                                AccountNature.DEBIT,
                                money("119.00"),
                                money("0.00"),
                                money("119.00")),
                        new LedgerAccountSummaryResult(
                                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                                "4135",
                                "Ingresos",
                                AccountNature.CREDIT,
                                money("0.00"),
                                money("100.00"),
                                money("100.00"))));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
