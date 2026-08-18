package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

class QueryAccountingBooksServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("13571357-1357-1357-1357-135713571357");
    private static final UUID THIRDPARTY_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final LocalDate FROM_DATE = LocalDate.of(2026, 5, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2026, 5, 31);

    @Test
    void journalBookListsPostedEntriesWithDetailAndPeriodTotals() {
        TestContext context = TestContext.withDefaultAccounts();
        AccountingEntry entry = saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta POS 1");
        context.entries.save(entry);

        JournalBookResult result = context.service().journalBook(new JournalBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.debitTotal()).isEqualByComparingTo("119.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("119.00");
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).entryDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(result.entries().get(0).description()).isEqualTo("Venta POS 1");
        assertThat(result.entries().get(0).sourceType()).isEqualTo(AccountingSourceType.SALE);
        assertThat(result.entries().get(0).sourceId()).isEqualTo(entry.sourceId());
        assertThat(result.entries().get(0).lines()).hasSize(3);
        assertThat(result.entries().get(0).lines().get(0).accountCode()).isEqualTo("1105");
        assertThat(result.entries().get(0).lines().get(0).thirdpartyId()).isEqualTo(THIRDPARTY_ID);
        assertThat(result.entries().get(0).lines().get(0).debitAmount()).isEqualByComparingTo("119.00");
    }

    @Test
    void journalBookExcludesEntriesOutsidePeriodAndOtherCompanies() {
        TestContext context = TestContext.withDefaultAccounts();
        context.accounts.saveDefaultAccountsFor(OTHER_COMPANY_ID);
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta incluida"));
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 4, 30), "Venta anterior"));
        context.entries.save(saleEntry(OTHER_COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta otra empresa"));

        JournalBookResult result = context.service().journalBook(new JournalBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).description()).isEqualTo("Venta incluida");
    }

    @Test
    void journalBookOrdersEntriesByDateDescriptionAndId() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 20), "Venta B"));
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 10), "Venta C"));
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 10), "Venta A"));

        JournalBookResult result = context.service().journalBook(new JournalBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.entries()).extracting("description").containsExactly("Venta A", "Venta C", "Venta B");
    }

    @Test
    void ledgerBookGroupsMovementsByAccount() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta POS 1"));
        context.entries.save(expenseEntry(COMPANY_ID, LocalDate.of(2026, 5, 20), "Gasto caja"));

        LedgerBookResult result = context.service().ledgerBook(new LedgerBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.debitTotal()).isEqualByComparingTo("169.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("169.00");
        assertThat(result.accounts()).extracting("accountCode").containsExactly("1105", "2408", "4135", "5135");
        LedgerAccountSummaryResult cash = account(result, "1105");
        assertThat(cash.nature()).isEqualTo(AccountNature.DEBIT);
        assertThat(cash.debitTotal()).isEqualByComparingTo("119.00");
        assertThat(cash.creditTotal()).isEqualByComparingTo("50.00");
        assertThat(cash.balance()).isEqualByComparingTo("69.00");
    }

    @Test
    void ledgerBookCalculatesCreditNatureBalanceAsCreditsMinusDebits() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta POS 1"));

        LedgerBookResult result = context.service().ledgerBook(new LedgerBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        LedgerAccountSummaryResult income = account(result, "4135");
        assertThat(income.nature()).isEqualTo(AccountNature.CREDIT);
        assertThat(income.debitTotal()).isEqualByComparingTo("0.00");
        assertThat(income.creditTotal()).isEqualByComparingTo("100.00");
        assertThat(income.balance()).isEqualByComparingTo("100.00");
    }

    @Test
    void incomeStatementSummarizesRevenueCostsAndExpenses() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta POS 1"));
        context.entries.save(expenseEntry(COMPANY_ID, LocalDate.of(2026, 5, 20), "Gasto caja"));

        var result = context.service().incomeStatement(new LedgerBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.statementType()).isEqualTo("INCOME_STATEMENT");
        assertThat(result.groups()).extracting("code").containsExactly("4", "6", "5", "7");
        assertThat(result.groups().get(0).total()).isEqualByComparingTo("100.00");
        assertThat(result.groups().get(2).total()).isEqualByComparingTo("50.00");
        assertThat(result.total()).isEqualByComparingTo("50.00");
    }

    @Test
    void balanceSheetSummarizesAssetsLiabilitiesAndEquity() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(saleEntry(COMPANY_ID, LocalDate.of(2026, 5, 15), "Venta POS 1"));

        var result = context.service().balanceSheet(new LedgerBookQuery(COMPANY_ID, FROM_DATE, TO_DATE));

        assertThat(result.statementType()).isEqualTo("BASIC_BALANCE_SHEET");
        assertThat(result.groups()).extracting("code").containsExactly("1", "2", "3");
        assertThat(result.groups().get(0).total()).isEqualByComparingTo("119.00");
        assertThat(result.groups().get(1).total()).isEqualByComparingTo("19.00");
        assertThat(result.total()).isEqualByComparingTo("100.00");
    }

    @Test
    void ledgerBookRejectsMissingAccountForSummary() {
        TestContext context = TestContext.withDefaultAccounts();
        context.entries.save(AccountingEntry.post(
                UUID.randomUUID(),
                COMPANY_ID,
                LocalDate.of(2026, 5, 15),
                "Asiento con cuenta inexistente",
                AccountingSourceType.ADJUSTMENT,
                UUID.randomUUID(),
                List.of(
                        line("1105", "Caja", money("10.00"), money("0.00")),
                        line("9999", "Cuenta inexistente", money("0.00"), money("10.00")))));

        assertThatThrownBy(() -> context.service().ledgerBook(new LedgerBookQuery(COMPANY_ID, FROM_DATE, TO_DATE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account was not found: 9999");
    }

    @Test
    void booksRejectInvalidDateRange() {
        TestContext context = TestContext.withDefaultAccounts();

        assertThatThrownBy(() -> context.service()
                .journalBook(new JournalBookQuery(COMPANY_ID, TO_DATE, FROM_DATE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fromDate cannot be after toDate");
        assertThatThrownBy(() -> context.service()
                .ledgerBook(new LedgerBookQuery(COMPANY_ID, TO_DATE, FROM_DATE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fromDate cannot be after toDate");
    }

    private static LedgerAccountSummaryResult account(LedgerBookResult result, String accountCode) {
        return result.accounts().stream()
                .filter(account -> account.accountCode().equals(accountCode))
                .findFirst()
                .orElseThrow();
    }

    private static AccountingEntry saleEntry(UUID companyId, LocalDate date, String description) {
        return AccountingEntry.post(
                UUID.randomUUID(),
                companyId,
                date,
                description,
                AccountingSourceType.SALE,
                UUID.randomUUID(),
                List.of(
                        line("1105", "Caja", money("119.00"), money("0.00")),
                        line("4135", "Comercio al por mayor y al por menor", money("0.00"), money("100.00")),
                        line("2408", "Impuesto sobre las ventas por pagar", money("0.00"), money("19.00"))));
    }

    private static AccountingEntry expenseEntry(UUID companyId, LocalDate date, String description) {
        return AccountingEntry.post(
                UUID.randomUUID(),
                companyId,
                date,
                description,
                AccountingSourceType.EXPENSE,
                UUID.randomUUID(),
                List.of(
                        line("5135", "Servicios", money("50.00"), money("0.00")),
                        line("1105", "Caja", money("0.00"), money("50.00"))));
    }

    private static AccountingEntryLine line(
            String accountCode,
            String accountName,
            BigDecimal debitAmount,
            BigDecimal creditAmount) {
        return AccountingEntryLine.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                accountCode,
                accountName,
                THIRDPARTY_ID,
                debitAmount,
                creditAmount,
                accountName);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static final class TestContext {

        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingEntryRepository entries = new InMemoryAccountingEntryRepository();

        static TestContext withDefaultAccounts() {
            TestContext context = new TestContext();
            context.accounts.saveDefaultAccountsFor(COMPANY_ID);
            return context;
        }

        QueryAccountingBooksService service() {
            return new QueryAccountingBooksService(entries, accounts);
        }
    }

    private static final class InMemoryAccountRepository implements AccountRepositoryPort {

        private final Map<String, Account> accounts = new HashMap<>();

        void saveDefaultAccountsFor(UUID companyId) {
            save(Account.create(UUID.randomUUID(), companyId, "1105", "Caja", null));
            save(Account.create(UUID.randomUUID(), companyId, "2408", "Impuesto sobre las ventas por pagar", null));
            save(Account.create(UUID.randomUUID(), companyId, "4135", "Comercio al por mayor y al por menor", null));
            save(Account.create(UUID.randomUUID(), companyId, "5135", "Servicios", null));
            save(Account.create(UUID.randomUUID(), companyId, "6", "Costos de venta", null));
            save(Account.create(UUID.randomUUID(), companyId, "7", "Costos de produccion", null));
        }

        @Override
        public Optional<Account> findByCompanyIdAndCode(UUID companyId, String code) {
            return Optional.ofNullable(accounts.get(key(companyId, code)));
        }


        @Override
        public List<Account> findByCompanyId(UUID companyId, Boolean active) {
            return accounts.values().stream()
                    .filter(account -> account.companyId().equals(companyId))
                    .filter(account -> active == null || account.active() == active)
                    .toList();
        }
        @Override
        public Account save(Account account) {
            accounts.put(key(account.companyId(), account.code()), account);
            return account;
        }

        private static String key(UUID companyId, String code) {
            return companyId + ":" + code;
        }
    }

    private static final class InMemoryAccountingEntryRepository implements AccountingEntryRepositoryPort {

        private final List<AccountingEntry> entries = new ArrayList<>();

        @Override
        public boolean existsByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
            return entries.stream()
                    .anyMatch(entry -> entry.companyId().equals(companyId)
                            && entry.sourceType() == sourceType
                            && entry.sourceId().equals(sourceId));
        }

        @Override
        public Optional<AccountingEntry> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
                UUID sourceId) {
            return entries.stream()
                    .filter(entry -> entry.companyId().equals(companyId)
                            && entry.sourceType() == sourceType
                            && entry.sourceId().equals(sourceId))
                    .findFirst();
        }

        @Override
        public AccountingEntry save(AccountingEntry entry) {
            entries.add(entry);
            return entry;
        }

        @Override
        public List<AccountingEntry> findPostedByCompanyIdAndEntryDateBetween(
                UUID companyId,
                LocalDate fromDate,
                LocalDate toDate) {
            return entries.stream()
                    .filter(entry -> entry.companyId().equals(companyId))
                    .filter(entry -> !entry.entryDate().isBefore(fromDate))
                    .filter(entry -> !entry.entryDate().isAfter(toDate))
                    .toList();
        }
    }
}
