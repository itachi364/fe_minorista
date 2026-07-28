package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

class AccountingRuleManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");

    @Test
    void replaceActiveDeactivatesPreviousRuleAndCreatesNewActiveRule() {
        TestContext context = TestContext.withDefaultAccounts();
        AccountingRuleManagementService service = context.service();
        AccountingRuleResult first = service.create(ruleCommand("Venta contado", "1105"));

        AccountingRuleResult replacement = service.replaceActive(ruleCommand("Venta cartera", "1305"));

        assertThat(replacement.active()).isTrue();
        assertThat(replacement.name()).isEqualTo("Venta cartera");
        assertThat(replacement.lines().get(0).accountCode()).isEqualTo("1305");
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, true))
                .extracting(AccountingRule::id)
                .containsExactly(replacement.id());
        assertThat(context.rules.findByCompanyId(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, false))
                .extracting(AccountingRule::id)
                .containsExactly(first.id());
    }

    @Test
    void deactivateActiveRuleMakesItUnavailableForPosting() {
        TestContext context = TestContext.withDefaultAccounts();
        AccountingRuleManagementService service = context.service();
        service.create(ruleCommand("Venta contado", "1105"));

        AccountingRuleResult result = service.deactivateActive(COMPANY_ID, AccountingEventType.SALE_CONFIRMED);

        assertThat(result.active()).isFalse();
        assertThat(context.rules.findActiveByCompanyIdAndEventType(COMPANY_ID, AccountingEventType.SALE_CONFIRMED))
                .isEmpty();
    }

    @Test
    void createStillRejectsDuplicateActiveRuleWhenNotReplacing() {
        TestContext context = TestContext.withDefaultAccounts();
        AccountingRuleManagementService service = context.service();
        service.create(ruleCommand("Venta contado", "1105"));

        assertThatThrownBy(() -> service.create(ruleCommand("Venta cartera", "1305")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("active accounting rule already exists for event type");
    }

    private static CreateAccountingRuleCommand ruleCommand(String name, String debitAccount) {
        return new CreateAccountingRuleCommand(COMPANY_ID, AccountingEventType.SALE_CONFIRMED, AccountingSourceType.SALE,
                name, List.of(
                        new CreateAccountingRuleLineCommand(debitAccount, AccountingEntrySide.DEBIT,
                                AccountingAmountType.TOTAL, debitAccount),
                        new CreateAccountingRuleLineCommand("4135", AccountingEntrySide.CREDIT,
                                AccountingAmountType.SUBTOTAL, "Ingresos"),
                        new CreateAccountingRuleLineCommand("2408", AccountingEntrySide.CREDIT,
                                AccountingAmountType.TAX_TOTAL, "IVA generado")));
    }

    private static final class TestContext {
        private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        private final InMemoryAccountingRuleRepository rules = new InMemoryAccountingRuleRepository();
        private final Queue<UUID> ids = new ArrayDeque<>();

        static TestContext withDefaultAccounts() {
            TestContext context = new TestContext();
            context.ids.add(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            context.ids.add(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "1105", "Caja", null));
            context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "1305", "Clientes", null));
            context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "4135", "Ingresos", null));
            context.accounts.save(Account.create(UUID.randomUUID(), COMPANY_ID, "2408", "IVA", null));
            return context;
        }

        AccountingRuleManagementService service() {
            return new AccountingRuleManagementService(rules, accounts, ids::remove);
        }
    }

    private static final class InMemoryAccountRepository implements AccountRepositoryPort {
        private final Map<String, Account> accounts = new HashMap<>();

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
        private final Map<UUID, AccountingRule> rules = new HashMap<>();

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