package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;

class BasicAccountingSetupServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");

    @Test
    void initializeCreatesBasicAccountsAndRules() {
        TestContext context = new TestContext();
        BasicAccountingSetupService service = context.service();

        var result = service.initialize(COMPANY_ID);

        assertThat(result.templateName()).isEqualTo("BASIC_COLOMBIA_SMALL_BUSINESS");
        assertThat(result.accounts()).extracting("code")
                .containsExactly("1105", "1110", "1305", "1435", "1520", "2205", "2408", "4135", "5105", "5135");
        assertThat(result.rules()).extracting("eventType").containsExactly(
                AccountingEventType.SALE_CONFIRMED,
                AccountingEventType.INVENTORY_REPLENISHMENT_CONFIRMED,
                AccountingEventType.PURCHASE_CONFIRMED,
                AccountingEventType.OPERATING_EXPENSE_CONFIRMED,
                AccountingEventType.ASSET_PURCHASE_CONFIRMED,
                AccountingEventType.ACCOUNT_RECEIVABLE_REGISTERED,
                AccountingEventType.ACCOUNTS_PAYABLE_PAYMENT_REGISTERED,
                AccountingEventType.ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED,
                AccountingEventType.PAYROLL_DAILY_PAYMENT_REGISTERED);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, null, true)).hasSize(9);
    }

    @Test
    void initializeReactivatesExistingBasicAccountAndPreservesActiveRules() {
        TestContext context = new TestContext();
        Account inactiveCash = Account.restore(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), COMPANY_ID,
                "1105", "Caja", null, false);
        context.accounts.save(inactiveCash);
        BasicAccountingSetupService service = context.service();
        var firstSaleRule = service.initialize(COMPANY_ID).rules().stream()
                .filter(rule -> rule.eventType() == AccountingEventType.SALE_CONFIRMED)
                .findFirst()
                .orElseThrow();

        var second = service.initialize(COMPANY_ID);

        assertThat(second.accounts()).filteredOn(account -> account.code().equals("1105"))
                .singleElement()
                .extracting("active")
                .isEqualTo(true);
        assertThat(context.accounts.findByCompanyId(COMPANY_ID, null)).hasSize(10);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, true)).hasSize(1);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, false)).isEmpty();
        assertThat(second.rules()).filteredOn(rule -> rule.eventType() == AccountingEventType.SALE_CONFIRMED)
                .singleElement()
                .extracting("id")
                .isEqualTo(firstSaleRule.id());
    }

    @Test
    void initializeCompletesMissingRulesWithoutReplacingActiveRules() {
        TestContext context = new TestContext();
        BasicAccountingSetupService service = context.service();
        var firstResult = service.initialize(COMPANY_ID);
        var firstSaleRule = firstResult.rules().stream()
                .filter(rule -> rule.eventType() == AccountingEventType.SALE_CONFIRMED)
                .findFirst()
                .orElseThrow();
        var firstReceivableRule = context.rules.findByCompanyId(COMPANY_ID,
                AccountingEventType.ACCOUNT_RECEIVABLE_REGISTERED, true).stream()
                .findFirst()
                .orElseThrow();
        context.rules.save(firstReceivableRule.deactivate());

        var second = service.initialize(COMPANY_ID);

        assertThat(second.rules()).hasSize(9);
        assertThat(second.rules()).filteredOn(rule -> rule.eventType() == AccountingEventType.SALE_CONFIRMED)
                .singleElement()
                .extracting("id")
                .isEqualTo(firstSaleRule.id());
        assertThat(second.rules()).filteredOn(rule -> rule.eventType() == AccountingEventType.ACCOUNT_RECEIVABLE_REGISTERED)
                .singleElement()
                .extracting("id")
                .isNotEqualTo(firstReceivableRule.id());
        assertThat(context.rules.findByCompanyId(COMPANY_ID, null, true)).hasSize(9);
        assertThat(context.rules.findByCompanyId(COMPANY_ID, null, false)).hasSize(1);
    }

    private static final class TestContext {
        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();
        private final Queue<UUID> ids = new ArrayDeque<>();

        TestContext() {
            for (int index = 0; index < 100; index++) {
                ids.add(UUID.nameUUIDFromBytes(("basic-accounting-setup-" + index).getBytes()));
            }
        }

        BasicAccountingSetupService service() {
            return new BasicAccountingSetupService(accounts, rules, ids::remove);
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
}
