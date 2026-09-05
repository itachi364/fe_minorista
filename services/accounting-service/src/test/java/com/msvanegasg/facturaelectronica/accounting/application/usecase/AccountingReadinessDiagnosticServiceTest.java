package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

class AccountingReadinessDiagnosticServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");

    @Test
    void reportsMissingActiveRuleForEvent() {
        TestContext context = new TestContext();

        var result = context.service().diagnose(COMPANY_ID, AccountingEventType.SALE_CONFIRMED);

        assertThat(result.ready()).isFalse();
        assertThat(result.accountingRuleId()).isNull();
        assertThat(result.missingItems()).extracting("code").containsExactly("ACCOUNTING_RULE_NOT_ACTIVE");
    }

    @Test
    void reportsMissingAccountsReferencedByActiveRule() {
        TestContext context = new TestContext();
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "1105", "Caja", null));
        context.rules.save(saleRule());

        var result = context.service().diagnose(COMPANY_ID, AccountingEventType.SALE_CONFIRMED);

        assertThat(result.ready()).isFalse();
        assertThat(result.accountingRuleId()).isNotNull();
        assertThat(result.checkedAccountCodes()).containsExactly("1105", "4135");
        assertThat(result.missingItems()).extracting("message")
                .containsExactly("La cuenta PUC 4135 no existe o esta inactiva.");
    }

    @Test
    void reportsReadyWhenRuleAndAccountsAreActive() {
        TestContext context = new TestContext();
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "1105", "Caja", null));
        context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "4135", "Ingresos", null));
        context.rules.save(saleRule());

        var result = context.service().diagnose(COMPANY_ID, AccountingEventType.SALE_CONFIRMED);

        assertThat(result.ready()).isTrue();
        assertThat(result.missingItems()).isEmpty();
        assertThat(result.checkedAccountCodes()).containsExactly("1105", "4135");
    }

    private static AccountingRule saleRule() {
        return AccountingRule.create(UUID.fromString("99999999-9999-9999-9999-999999999999"), COMPANY_ID,
                AccountingEventType.SALE_CONFIRMED, AccountingSourceType.SALE, "Venta facturada", List.of(
                        AccountingRuleLine.create("1105", AccountingEntrySide.DEBIT, AccountingAmountType.TOTAL,
                                "Caja"),
                        AccountingRuleLine.create("4135", AccountingEntrySide.CREDIT,
                                AccountingAmountType.SUBTOTAL, "Ingresos")));
    }

    private static final class TestContext {
        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();

        AccountingReadinessDiagnosticService service() {
            return new AccountingReadinessDiagnosticService(rules, accounts);
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
}
