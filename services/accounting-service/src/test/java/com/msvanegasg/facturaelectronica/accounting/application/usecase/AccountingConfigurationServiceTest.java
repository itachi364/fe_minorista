package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingConfigurationCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

class AccountingConfigurationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");

    @Test
    void configuresAccountsAndRulesInSingleBatch() {
        TestContext context = new TestContext();
        AccountingConfigurationService service = context.service();

        AccountingSetupResult result = service.configure(new AccountingConfigurationCommand(COMPANY_ID,
                List.of(
                        new CreateAccountCommand(COMPANY_ID, "1105", "Caja", null),
                        new CreateAccountCommand(COMPANY_ID, "4135", "Ingresos", null),
                        new CreateAccountCommand(COMPANY_ID, "2408", "IVA", null)),
                List.of(saleRule())));

        assertThat(result.templateName()).isEqualTo("CUSTOM_ACCOUNTING_CONFIGURATION");
        assertThat(result.accounts()).extracting("code").containsExactly("1105", "4135", "2408");
        assertThat(result.rules()).extracting("eventType").containsExactly(AccountingEventType.SALE_CONFIRMED);
        assertThat(context.rules.findActiveByCompanyIdAndEventType(COMPANY_ID, AccountingEventType.SALE_CONFIRMED))
                .isPresent();
    }

    @Test
    void rejectsInvalidRuleBeforePersistingAccounts() {
        TestContext context = new TestContext();
        AccountingConfigurationService service = context.service();

        assertThatThrownBy(() -> service.configure(new AccountingConfigurationCommand(COMPANY_ID,
                List.of(new CreateAccountCommand(COMPANY_ID, "1105", "Caja", null)),
                List.of(new CreateAccountingRuleCommand(COMPANY_ID, AccountingEventType.SALE_CONFIRMED,
                        AccountingSourceType.SALE, "Venta invalida", List.of(
                                new CreateAccountingRuleLineCommand("9999", AccountingEntrySide.DEBIT,
                                        AccountingAmountType.TOTAL, "Cuenta inexistente"),
                                new CreateAccountingRuleLineCommand("1105", AccountingEntrySide.CREDIT,
                                        AccountingAmountType.TOTAL, "Caja")))))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account was not found: 9999");

        assertThat(context.accounts.findByCompanyId(COMPANY_ID, null)).isEmpty();
        assertThat(context.rules.findByCompanyId(COMPANY_ID, null, null)).isEmpty();
    }

    @Test
    void rejectsReplacingUsedActiveRule() {
        TestContext context = new TestContext();
        AccountingRule activeRule = AccountingRule.create(UUID.fromString("99999999-9999-9999-9999-999999999999"),
                COMPANY_ID, AccountingEventType.SALE_CONFIRMED, AccountingSourceType.SALE, "Venta usada",
                List.of(
                        com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine.create("1105",
                                AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL, "Caja"),
                        com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine.create("4135",
                                AccountingEntrySide.CREDIT, AccountingAmountType.SUBTOTAL, "Ingresos")));
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "1105", "Caja", null));
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "4135", "Ingresos", null));
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "2408", "IVA", null));
        context.rules.save(activeRule);
        context.entries.markRuleUsed(activeRule.id());
        AccountingConfigurationService service = context.service();

        assertThatThrownBy(() -> service.configure(new AccountingConfigurationCommand(COMPANY_ID, List.of(),
                List.of(saleRule()))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("used accounting rule cannot be replaced");
    }

    private static CreateAccountingRuleCommand saleRule() {
        return new CreateAccountingRuleCommand(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, AccountingSourceType.SALE,
                "Venta facturada", List.of(
                        new CreateAccountingRuleLineCommand("1105", AccountingEntrySide.DEBIT,
                                AccountingAmountType.TOTAL, "Caja"),
                        new CreateAccountingRuleLineCommand("4135", AccountingEntrySide.CREDIT,
                                AccountingAmountType.SUBTOTAL, "Ingresos"),
                        new CreateAccountingRuleLineCommand("2408", AccountingEntrySide.CREDIT,
                                AccountingAmountType.TAX_TOTAL, "IVA")));
    }

    private static final class TestContext {
        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();
        private final InMemoryAccountingEntryRepository entries = new InMemoryAccountingEntryRepository();
        private final Queue<UUID> ids = new ArrayDeque<>(List.of(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("44444444-4444-4444-4444-444444444444")));

        AccountingConfigurationService service() {
            return new AccountingConfigurationService(accounts, rules, entries, ids::remove);
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

        @Override
        public Optional<Account> findByCompanyIdAndCode(UUID companyId, String code) {
            return Optional.ofNullable(accounts.get(key(companyId, code)));
        }

        @Override
        public List<Account> findByCompanyId(UUID companyId, Boolean active) {
            return accounts.values().stream()
                    .filter(account -> account.companyId().equals(companyId))
                    .filter(account -> active == null || account.active() == active)
                    .sorted(Comparator.comparing(Account::code))
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
        private final Map<UUID, AccountingRule> rules = new HashMap<>();

        @Override
        public Optional<AccountingRule> findByCompanyIdAndId(UUID companyId, UUID id) {
            return rules.values().stream()
                    .filter(rule -> rule.companyId().equals(companyId))
                    .filter(rule -> rule.id().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<AccountingRule> findActiveByCompanyIdAndEventType(UUID companyId, AccountingEventType eventType) {
            return rules.values().stream()
                    .filter(rule -> rule.companyId().equals(companyId))
                    .filter(rule -> rule.eventType() == eventType)
                    .filter(AccountingRule::active)
                    .findFirst();
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
            rules.put(rule.id(), rule);
            return rule;
        }
    }

    private static final class InMemoryAccountingEntryRepository implements AccountingEntryRepositoryPort {
        private final Map<UUID, Long> ruleUsage = new HashMap<>();

        void markRuleUsed(UUID ruleId) {
            ruleUsage.merge(ruleId, 1L, Long::sum);
        }

        @Override
        public boolean existsByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
            return false;
        }

        @Override
        public long countByAccountId(UUID accountId) {
            return 0;
        }

        @Override
        public long countByAccountingRuleId(UUID accountingRuleId) {
            return ruleUsage.getOrDefault(accountingRuleId, 0L);
        }

        @Override
        public Optional<AccountingEntry> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
                UUID sourceId) {
            return Optional.empty();
        }

        @Override
        public AccountingEntry save(AccountingEntry entry) {
            throw new UnsupportedOperationException("not needed");
        }

        @Override
        public List<AccountingEntry> findPostedByCompanyIdAndEntryDateBetween(UUID companyId, LocalDate fromDate,
                LocalDate toDate) {
            return List.of();
        }
    }
}
