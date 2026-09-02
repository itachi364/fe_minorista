package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class GenerateAccountingEntryServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("13571357-1357-1357-1357-135713571357");
    private static final UUID SOURCE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID THIRDPARTY_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2026, 5, 15);

    @Test
    void generatePostsBalancedSaleEntryUsingCompanyRuleTemplate() {
        TestContext context = TestContext.withDefaultAccounts();
        AccountingRule rule = saleRule(COMPANY_ID);
        context.rules.save(rule);
        GenerateAccountingEntryService service = context.service();

        AccountingEntryResult result = service.generate(saleCommand(COMPANY_ID, SOURCE_ID));

        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.accountingRuleId()).isEqualTo(rule.id());
        assertThat(result.sourceType()).isEqualTo(AccountingSourceType.SALE);
        assertThat(result.sourceId()).isEqualTo(SOURCE_ID);
        assertThat(result.status()).isEqualTo(AccountingEntryStatus.POSTED);
        assertThat(result.debitTotal()).isEqualByComparingTo("119.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("119.00");
        assertThat(result.lines()).hasSize(3);
        assertThat(result.lines()).extracting("accountCode").containsExactly("1105", "4135", "2408");
        assertThat(result.lines().get(0).debitAmount()).isEqualByComparingTo("119.00");
        assertThat(result.lines().get(1).creditAmount()).isEqualByComparingTo("100.00");
        assertThat(result.lines().get(2).creditAmount()).isEqualByComparingTo("19.00");
        assertThat(context.entries.existsByCompanyIdAndSource(COMPANY_ID, AccountingSourceType.SALE, SOURCE_ID)).isTrue();
    }

    @Test
    void generatePostsBalancedPurchaseEntryWithConfigurableInventoryAndTaxAccounts() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(purchaseRule(COMPANY_ID));
        GenerateAccountingEntryService service = context.service();

        AccountingEntryResult result = service.generate(new GenerateAccountingEntryCommand(
                COMPANY_ID,
                AccountingEventType.PURCHASE_CONFIRMED,
                AccountingSourceType.PURCHASE,
                SOURCE_ID,
                ENTRY_DATE,
                "Compra de inventario",
                THIRDPARTY_ID,
                money("200.00"),
                money("38.00"),
                money("238.00")));

        assertThat(result.debitTotal()).isEqualByComparingTo("238.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("238.00");
        assertThat(result.lines()).extracting("accountCode").containsExactly("1435", "2408", "2205");
        assertThat(result.lines().get(0).debitAmount()).isEqualByComparingTo("200.00");
        assertThat(result.lines().get(1).debitAmount()).isEqualByComparingTo("38.00");
        assertThat(result.lines().get(2).creditAmount()).isEqualByComparingTo("238.00");
    }

    @Test
    void generatePostsExpenseEntryAndSkipsZeroTaxLine() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(expenseRule(COMPANY_ID));
        GenerateAccountingEntryService service = context.service();

        AccountingEntryResult result = service.generate(new GenerateAccountingEntryCommand(
                COMPANY_ID,
                AccountingEventType.EXPENSE_CONFIRMED,
                AccountingSourceType.EXPENSE,
                SOURCE_ID,
                ENTRY_DATE,
                "Gasto sin IVA",
                THIRDPARTY_ID,
                money("50.00"),
                money("0.00"),
                money("50.00")));

        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines()).extracting("accountCode").containsExactly("5135", "1105");
        assertThat(result.debitTotal()).isEqualByComparingTo("50.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void generatePostsDailyPayrollPaymentEntryWithoutTax() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(dailyPayrollRule(COMPANY_ID));
        GenerateAccountingEntryService service = context.service();

        AccountingEntryResult result = service.generate(new GenerateAccountingEntryCommand(
                COMPANY_ID,
                AccountingEventType.PAYROLL_DAILY_PAYMENT_REGISTERED,
                AccountingSourceType.PAYROLL_DAILY_PAYMENT,
                SOURCE_ID,
                ENTRY_DATE,
                "Pago diario verbal",
                THIRDPARTY_ID,
                money("80000.00"),
                money("0.00"),
                money("80000.00")));

        assertThat(result.sourceType()).isEqualTo(AccountingSourceType.PAYROLL_DAILY_PAYMENT);
        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines()).extracting("accountCode").containsExactly("5105", "1105");
        assertThat(result.lines().get(0).debitAmount()).isEqualByComparingTo("80000.00");
        assertThat(result.lines().get(1).creditAmount()).isEqualByComparingTo("80000.00");
        assertThat(result.debitTotal()).isEqualByComparingTo("80000.00");
        assertThat(result.creditTotal()).isEqualByComparingTo("80000.00");
    }

    @Test
    void generateRejectsUnbalancedRule() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(AccountingRule.create(
                UUID.randomUUID(),
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta descuadrada",
                List.of(
                        ruleLine("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL),
                        ruleLine("4135", AccountingEntrySide.CREDIT, AccountingAmountType.TAX_TOTAL))));
        GenerateAccountingEntryService service = context.service();

        assertThatThrownBy(() -> service.generate(saleCommand(COMPANY_ID, SOURCE_ID)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("accounting entry must be balanced");
    }

    @Test
    void generateRejectsMissingRuleForCompany() {
        TestContext context = TestContext.withDefaultAccounts();
        GenerateAccountingEntryService service = context.service();

        assertThatThrownBy(() -> service.generate(saleCommand(COMPANY_ID, SOURCE_ID)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("accounting rule was not found: SALE_CONFIRMED");
    }

    @Test
    void generateRejectsMissingAccountConfiguredInRule() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(AccountingRule.create(
                UUID.randomUUID(),
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta con cuenta inexistente",
                List.of(
                        ruleLine("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL),
                        ruleLine("999999", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL))));
        GenerateAccountingEntryService service = context.service();

        assertThatThrownBy(() -> service.generate(saleCommand(COMPANY_ID, SOURCE_ID)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account was not found: 999999");
    }

    @Test
    void generateRejectsInactiveAccountConfiguredInRule() {
        TestContext context = TestContext.withDefaultAccounts();
        context.accounts.save(Account.restore(
                UUID.randomUUID(),
                COMPANY_ID,
                "1305",
                "Clientes",
                null,
                false));
        context.rules.save(AccountingRule.create(
                UUID.randomUUID(),
                COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta con cartera inactiva",
                List.of(
                        ruleLine("1305", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL),
                        ruleLine("4135", AccountingEntrySide.CREDIT, AccountingAmountType.SUBTOTAL),
                        ruleLine("2408", AccountingEntrySide.CREDIT, AccountingAmountType.TAX_TOTAL))));
        GenerateAccountingEntryService service = context.service();

        assertThatThrownBy(() -> service.generate(saleCommand(COMPANY_ID, SOURCE_ID)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account is inactive: 1305");
    }

    @Test
    void generateIsIdempotentForAlreadyPostedSourceDocumentForSameCompany() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(saleRule(COMPANY_ID));
        GenerateAccountingEntryService service = context.service();
        AccountingEntryResult first = service.generate(saleCommand(COMPANY_ID, SOURCE_ID));

        AccountingEntryResult second = service.generate(saleCommand(COMPANY_ID, SOURCE_ID));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.debitTotal()).isEqualByComparingTo(first.debitTotal());
        assertThat(second.creditTotal()).isEqualByComparingTo(first.creditTotal());
    }

    @Test
    void generateAllowsSameSourceIdForDifferentCompanies() {
        TestContext context = TestContext.withDefaultAccounts();
        context.accounts.saveDefaultAccountsFor(OTHER_COMPANY_ID);
        context.rules.save(saleRule(COMPANY_ID));
        context.rules.save(saleRule(OTHER_COMPANY_ID));
        GenerateAccountingEntryService service = context.service();

        service.generate(saleCommand(COMPANY_ID, SOURCE_ID));
        AccountingEntryResult otherCompanyResult = service.generate(saleCommand(OTHER_COMPANY_ID, SOURCE_ID));

        assertThat(otherCompanyResult.companyId()).isEqualTo(OTHER_COMPANY_ID);
        assertThat(otherCompanyResult.sourceId()).isEqualTo(SOURCE_ID);
    }
    @Test
    void generatePublishesAccountingEntryPostedEvent() {
        TestContext context = TestContext.withDefaultAccounts();
        context.rules.save(saleRule(COMPANY_ID));
        CapturingPublisher publisher = new CapturingPublisher();
        GenerateAccountingEntryService service = context.service(publisher);

        AccountingEntryResult result = service.generate(saleCommand(COMPANY_ID, SOURCE_ID));

        assertThat(publisher.events).hasSize(1);
        DomainEventEnvelope event = publisher.events.get(0);
        assertThat(event.eventType()).isEqualTo(EventTypes.ACCOUNTING_ENTRY_POSTED);
        assertThat(event.companyId()).isEqualTo(COMPANY_ID);
        assertThat(event.aggregateType()).isEqualTo("AccountingEntry");
        assertThat(event.aggregateId()).isEqualTo(result.id());
        assertThat(event.idempotencyKey()).isEqualTo("SALE:" + SOURCE_ID + ":accounting-entry-posted");
        assertThat(event.payload()).containsEntry("sourceType", "SALE");
        assertThat(event.payload()).containsEntry("sourceId", SOURCE_ID.toString());
    }

    @Test
    void entryLineRejectsDebitAndCreditAtTheSameTime() {
        assertThatThrownBy(() -> AccountingEntryLine.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "1105",
                "Caja",
                THIRDPARTY_ID,
                money("10.00"),
                money("10.00"),
                "Linea invalida"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accounting line cannot have debit and credit amounts at the same time");
    }

    @Test
    void entryLineRejectsLineWithoutAmount() {
        assertThatThrownBy(() -> AccountingEntryLine.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "1105",
                "Caja",
                THIRDPARTY_ID,
                money("0.00"),
                money("0.00"),
                "Linea sin valor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accounting line requires debit or credit amount");
    }

    private static GenerateAccountingEntryCommand saleCommand(UUID companyId, UUID sourceId) {
        return new GenerateAccountingEntryCommand(
                companyId,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                sourceId,
                ENTRY_DATE,
                "Venta POS FV-1",
                THIRDPARTY_ID,
                money("100.00"),
                money("19.00"),
                money("119.00"));
    }

    private static AccountingRule saleRule(UUID companyId) {
        return AccountingRule.create(
                UUID.randomUUID(),
                companyId,
                AccountingEventType.SALE_CONFIRMED,
                AccountingSourceType.SALE,
                "Venta POS/factura",
                List.of(
                        ruleLine("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL),
                        ruleLine("4135", AccountingEntrySide.CREDIT, AccountingAmountType.SUBTOTAL),
                        ruleLine("2408", AccountingEntrySide.CREDIT, AccountingAmountType.TAX_TOTAL)));
    }

    private static AccountingRule purchaseRule(UUID companyId) {
        return AccountingRule.create(
                UUID.randomUUID(),
                companyId,
                AccountingEventType.PURCHASE_CONFIRMED,
                AccountingSourceType.PURCHASE,
                "Compra de inventario",
                List.of(
                        ruleLine("1435", AccountingEntrySide.DEBIT, AccountingAmountType.SUBTOTAL),
                        ruleLine("2408", AccountingEntrySide.DEBIT, AccountingAmountType.TAX_TOTAL),
                        ruleLine("2205", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL)));
    }

    private static AccountingRule expenseRule(UUID companyId) {
        return AccountingRule.create(
                UUID.randomUUID(),
                companyId,
                AccountingEventType.EXPENSE_CONFIRMED,
                AccountingSourceType.EXPENSE,
                "Gasto administrativo",
                List.of(
                        ruleLine("5135", AccountingEntrySide.DEBIT, AccountingAmountType.SUBTOTAL),
                        ruleLine("2408", AccountingEntrySide.DEBIT, AccountingAmountType.TAX_TOTAL),
                        ruleLine("1105", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL)));
    }

    private static AccountingRule dailyPayrollRule(UUID companyId) {
        return AccountingRule.create(
                UUID.randomUUID(),
                companyId,
                AccountingEventType.PAYROLL_DAILY_PAYMENT_REGISTERED,
                AccountingSourceType.PAYROLL_DAILY_PAYMENT,
                "Pago diario verbal",
                List.of(
                        ruleLine("5105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL),
                        ruleLine("1105", AccountingEntrySide.CREDIT, AccountingAmountType.TOTAL)));
    }

    private static AccountingRuleLine ruleLine(
            String accountCode,
            AccountingEntrySide side,
            AccountingAmountType amountType) {
        return AccountingRuleLine.create(accountCode, side, amountType, accountCode);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }


    private static final class CapturingPublisher implements DomainEventPublisherPort {

        private final List<DomainEventEnvelope> events = new ArrayList<>();

        @Override
        public void publish(DomainEventEnvelope event) {
            events.add(event);
        }
    }
    private static final class TestContext {

        private final InMemoryAccountRepository accounts;
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();
        private final InMemoryAccountingEntryRepository entries = new InMemoryAccountingEntryRepository();
        private final IdGeneratorPort idGenerator = new QueueIdGenerator();

        private TestContext(InMemoryAccountRepository accounts) {
            this.accounts = accounts;
        }

        static TestContext withDefaultAccounts() {
            InMemoryAccountRepository accounts = new InMemoryAccountRepository();
            accounts.saveDefaultAccountsFor(COMPANY_ID);
            return new TestContext(accounts);
        }

        GenerateAccountingEntryService service() {
            return new GenerateAccountingEntryService(rules, accounts, entries, idGenerator);
        }
        GenerateAccountingEntryService service(DomainEventPublisherPort publisher) {
            return new GenerateAccountingEntryService(rules, accounts, entries, publisher, idGenerator,
                    Clock.fixed(Instant.parse("2026-05-19T10:00:00Z"), ZoneOffset.UTC));
        }
    }

    private static final class InMemoryAccountRepository implements AccountRepositoryPort {

        private final Map<String, Account> accounts = new HashMap<>();

        @Override
        public Optional<Account> findByCompanyIdAndId(UUID companyId, UUID id) {
            return accounts.values().stream()
                    .filter(account -> account.companyId().equals(companyId))
                    .filter(account -> account.id().equals(id))
                    .findFirst();
        }

        void saveDefaultAccountsFor(UUID companyId) {
            save(Account.create(UUID.randomUUID(), companyId, "1105", "Caja", null));
            save(Account.create(UUID.randomUUID(), companyId, "1435", "Inventarios", null));
            save(Account.create(UUID.randomUUID(), companyId, "2205", "Proveedores nacionales", null));
            save(Account.create(UUID.randomUUID(), companyId, "2408", "Impuesto sobre las ventas por pagar", null));
            save(Account.create(UUID.randomUUID(), companyId, "4135", "Comercio al por mayor y al por menor", null));
            save(Account.create(UUID.randomUUID(), companyId, "5105", "Gastos de personal", null));
            save(Account.create(UUID.randomUUID(), companyId, "5135", "Servicios", null));
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

    private static final class InMemoryAccountingRuleRepository implements AccountingRuleRepositoryPort {

        private final Map<String, AccountingRule> rules = new HashMap<>();

        private void put(AccountingRule rule) {
            rules.put(key(rule.companyId(), rule.eventType()), rule);
        }

        @Override
        public Optional<AccountingRule> findByCompanyIdAndId(UUID companyId, UUID id) {
            return rules.values().stream()
                    .filter(rule -> rule.companyId().equals(companyId))
                    .filter(rule -> rule.id().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<AccountingRule> findActiveByCompanyIdAndEventType(
                UUID companyId,
                AccountingEventType eventType) {
            return Optional.ofNullable(rules.get(key(companyId, eventType))).filter(AccountingRule::active);
        }


        @Override
        public List<AccountingRule> findByCompanyId(UUID companyId, AccountingEventType eventType, Boolean active) {
            return rules.values().stream()
                    .filter(rule -> rule.companyId().equals(companyId))
                    .filter(rule -> eventType == null || rule.eventType() == eventType)
                    .filter(rule -> active == null || rule.active() == active)
                    .toList();
        }
        @Override
        public AccountingRule save(AccountingRule rule) {
            put(rule);
            return rule;
        }

        private static String key(UUID companyId, AccountingEventType eventType) {
            return companyId + ":" + eventType;
        }
    }

    private static final class InMemoryAccountingEntryRepository implements AccountingEntryRepositoryPort {

        private final Map<String, AccountingEntry> entries = new HashMap<>();

        @Override
        public boolean existsByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
            return entries.containsKey(key(companyId, sourceType, sourceId));
        }

        @Override
        public long countByAccountId(UUID accountId) {
            return entries.values().stream()
                    .flatMap(entry -> entry.lines().stream())
                    .filter(line -> line.accountId().equals(accountId))
                    .count();
        }

        @Override
        public long countByAccountingRuleId(UUID accountingRuleId) {
            return entries.values().stream()
                    .filter(entry -> accountingRuleId.equals(entry.accountingRuleId()))
                    .count();
        }

        @Override
        public Optional<AccountingEntry> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
                UUID sourceId) {
            return Optional.ofNullable(entries.get(key(companyId, sourceType, sourceId)));
        }

        @Override
        public AccountingEntry save(AccountingEntry entry) {
            entries.put(key(entry.companyId(), entry.sourceType(), entry.sourceId()), entry);
            return entry;
        }

        @Override
        public List<AccountingEntry> findPostedByCompanyIdAndEntryDateBetween(
                UUID companyId,
                LocalDate fromDate,
                LocalDate toDate) {
            return new ArrayList<>(entries.values()).stream()
                    .filter(entry -> entry.companyId().equals(companyId))
                    .filter(entry -> !entry.entryDate().isBefore(fromDate))
                    .filter(entry -> !entry.entryDate().isAfter(toDate))
                    .toList();
        }

        private static String key(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
            return companyId + ":" + sourceType + ":" + sourceId;
        }
    }

    private static final class QueueIdGenerator implements IdGeneratorPort {

        private final Queue<UUID> ids = new ArrayDeque<>();

        QueueIdGenerator() {
            for (int index = 0; index < 200; index++) {
                ids.add(UUID.nameUUIDFromBytes(("accounting-test-id-" + index).getBytes()));
            }
        }

        @Override
        public UUID newId() {
            return ids.remove();
        }
    }
}
