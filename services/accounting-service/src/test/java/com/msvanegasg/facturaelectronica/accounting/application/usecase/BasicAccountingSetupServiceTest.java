package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

class BasicAccountingSetupServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");

    @Test
    void initializeCreatesBasicAccountsAndRules() {
        TestContext context = new TestContext();
        BasicAccountingSetupService service = context.service();

        var result = service.initialize(COMPANY_ID);

        assertThat(result.templateName()).isEqualTo("BASIC_COLOMBIA_SMALL_BUSINESS");
        assertThat(result.accounts()).extracting("code")
                .containsExactly("1105", "1110", "1305", "1435", "2205", "2408", "4135", "5105", "5135");
        assertThat(result.rules()).extracting("eventType").containsExactly(
                AccountingEventType.SALE_CONFIRMED,
                AccountingEventType.PURCHASE_CONFIRMED,
                AccountingEventType.EXPENSE_CONFIRMED,
                AccountingEventType.ACCOUNTS_PAYABLE_PAYMENT_REGISTERED,
                AccountingEventType.ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED,
                AccountingEventType.PAYROLL_DAILY_PAYMENT_REGISTERED);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, null, true)).hasSize(6);
    }

    @Test
    void initializeReactivatesExistingBasicAccountAndReplacesActiveRules() {
        TestContext context = new TestContext();
        Account inactiveCash = Account.restore(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), COMPANY_ID,
                "1105", "Caja", null, false);
        context.accounts.save(inactiveCash);
        BasicAccountingSetupService service = context.service();
        service.initialize(COMPANY_ID);

        var second = service.initialize(COMPANY_ID);

        assertThat(second.accounts()).filteredOn(account -> account.code().equals("1105"))
                .singleElement()
                .extracting("active")
                .isEqualTo(true);
        assertThat(context.accounts.findByCompanyId(COMPANY_ID, null)).hasSize(9);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, true)).hasSize(1);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, false)).hasSize(1);
    }

    @Test
    void initializeRejectsReplacingUsedActiveRule() {
        TestContext context = new TestContext();
        BasicAccountingSetupService service = context.service();
        var first = service.initialize(COMPANY_ID).rules().stream()
                .filter(rule -> rule.eventType() == AccountingEventType.SALE_CONFIRMED)
                .findFirst()
                .orElseThrow();
        context.entries.markRuleUsed(first.id());

        assertThatThrownBy(() -> service.initialize(COMPANY_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("used accounting rule cannot be replaced by basic setup");
    }

    private static final class TestContext {
        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();
        private final InMemoryAccountingEntryRepository entries = new InMemoryAccountingEntryRepository();
        private final Queue<UUID> ids = new ArrayDeque<>();

        TestContext() {
            for (int index = 0; index < 100; index++) {
                ids.add(UUID.nameUUIDFromBytes(("basic-accounting-setup-" + index).getBytes()));
            }
        }

        BasicAccountingSetupService service() {
            return new BasicAccountingSetupService(accounts, rules, entries, ids::remove);
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
                    .sorted(java.util.Comparator.comparing(Account::code))
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
