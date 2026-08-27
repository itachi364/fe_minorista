package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountLevel;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;

class ChartOfAccountsServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("aaaaaaaa-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID PARENT_ACCOUNT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Test
    void createClassifiesPucAssetAccountWithDebitNature() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());

        AccountResult result = service.create(new CreateAccountCommand(
                COMPANY_ID,
                "1105",
                "Caja",
                PARENT_ACCOUNT_ID));

        assertThat(result.id()).isEqualTo(ACCOUNT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.code()).isEqualTo("1105");
        assertThat(result.name()).isEqualTo("Caja");
        assertThat(result.category()).isEqualTo(AccountCategory.ASSET);
        assertThat(result.level()).isEqualTo(AccountLevel.ACCOUNT);
        assertThat(result.nature()).isEqualTo(AccountNature.DEBIT);
        assertThat(result.parentAccountId()).isEqualTo(PARENT_ACCOUNT_ID);
        assertThat(result.active()).isTrue();
    }

    @Test
    void createClassifiesIncomeAccountWithCreditNature() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());

        AccountResult result = service.create(new CreateAccountCommand(
                COMPANY_ID,
                "4135",
                "Comercio al por mayor y al por menor",
                null));

        assertThat(result.category()).isEqualTo(AccountCategory.INCOME);
        assertThat(result.nature()).isEqualTo(AccountNature.CREDIT);
    }

    @Test
    void createDeterminesSubaccountAndAuxiliaryLevels() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());

        AccountResult subaccount = service.create(new CreateAccountCommand(
                COMPANY_ID,
                "110505",
                "Caja general",
                null));
        AccountResult auxiliary = service.create(new CreateAccountCommand(
                COMPANY_ID,
                "11050501",
                "Caja general sede principal",
                subaccount.id()));

        assertThat(subaccount.level()).isEqualTo(AccountLevel.SUBACCOUNT);
        assertThat(auxiliary.level()).isEqualTo(AccountLevel.AUXILIARY);
    }


    @Test
    void findListsAccountsByCompanyAndActiveFlag() {
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        ChartOfAccountsService service = service(repository);
        service.create(new CreateAccountCommand(COMPANY_ID, "4135", "Ingresos", null));
        repository.save(Account.restore(UUID.randomUUID(), COMPANY_ID, "1105", "Caja", null, false));

        List<AccountResult> activeAccounts = service.find(COMPANY_ID, true);
        List<AccountResult> allAccounts = service.find(COMPANY_ID, null);

        assertThat(activeAccounts).extracting(AccountResult::code).containsExactly("4135");
        assertThat(allAccounts).extracting(AccountResult::code).containsExactly("1105", "4135");
    }

    @Test
    void createAllStoresSeveralAccountsAtOnce() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());

        List<AccountResult> results = service.createAll(List.of(
                new CreateAccountCommand(COMPANY_ID, "1105", "Caja", null),
                new CreateAccountCommand(COMPANY_ID, "4135", "Ingresos", null)));

        assertThat(results).extracting(AccountResult::code).containsExactly("1105", "4135");
    }

    @Test
    void createAllRejectsDuplicatedCodesBeforeSavingBatch() {
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        ChartOfAccountsService service = service(repository);

        assertThatThrownBy(() -> service.createAll(List.of(
                new CreateAccountCommand(COMPANY_ID, "1105", "Caja", null),
                new CreateAccountCommand(COMPANY_ID, "1105", "Caja duplicada", null))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("duplicated account code in batch: 1105");

        assertThat(repository.findByCompanyId(COMPANY_ID, null)).isEmpty();
    }

    @Test
    void createRejectsInvalidPucCode() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());

        assertThatThrownBy(() -> service.create(new CreateAccountCommand(
                COMPANY_ID,
                "A105",
                "Cuenta invalida",
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("account code must contain only digits");
    }

    @Test
    void createRejectsDuplicateAccountCodeForCompany() {
        ChartOfAccountsService service = service(new InMemoryAccountRepository());
        service.create(new CreateAccountCommand(COMPANY_ID, "1105", "Caja", null));

        assertThatThrownBy(() -> service.create(new CreateAccountCommand(COMPANY_ID, "1105", "Caja duplicada", null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account code already exists for company");
    }

    private static ChartOfAccountsService service(AccountRepositoryPort repository) {
        return new ChartOfAccountsService(repository, () -> ACCOUNT_ID);
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
}
