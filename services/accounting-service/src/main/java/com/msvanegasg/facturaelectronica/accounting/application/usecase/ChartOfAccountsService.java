package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageChartOfAccountsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;

public class ChartOfAccountsService implements ManageChartOfAccountsUseCase {

    private final AccountRepositoryPort accountRepository;
    private final IdGeneratorPort idGenerator;

    public ChartOfAccountsService(AccountRepositoryPort accountRepository, IdGeneratorPort idGenerator) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public AccountResult create(CreateAccountCommand command) {
        validate(command);
        String normalizedCode = command.code().trim();
        accountRepository.findByCompanyIdAndCode(command.companyId(), normalizedCode)
                .ifPresent(account -> {
                    throw new IllegalStateException("account code already exists for company");
                });

        Account account = Account.create(
                idGenerator.newId(),
                command.companyId(),
                normalizedCode,
                command.name(),
                command.parentAccountId());

        return toResult(accountRepository.save(account));
    }

    @Override
    public List<AccountResult> createAll(List<CreateAccountCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("accounts batch requires at least one account");
        }
        Set<String> requestedCodes = new HashSet<>();
        List<Account> accounts = commands.stream()
                .map(command -> {
                    validate(command);
                    Account account = Account.create(idGenerator.newId(), command.companyId(), command.code(),
                            command.name(), command.parentAccountId());
                    if (!requestedCodes.add(command.companyId() + ":" + account.code())) {
                        throw new IllegalStateException("duplicated account code in batch: " + account.code());
                    }
                    accountRepository.findByCompanyIdAndCode(command.companyId(), account.code())
                            .ifPresent(existing -> {
                                throw new IllegalStateException("account code already exists for company");
                            });
                    return account;
                })
                .toList();
        return accounts.stream()
                .map(accountRepository::save)
                .map(this::toResult)
                .toList();
    }

    @Override
    public AccountResult findByCode(UUID companyId, String code) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(code, "code is required");
        return accountRepository.findByCompanyIdAndCode(companyId, code.trim())
                .map(this::toResult)
                .orElseThrow(() -> new IllegalStateException("account was not found"));
    }

    @Override
    public List<AccountResult> find(UUID companyId, Boolean active) {
        Objects.requireNonNull(companyId, "companyId is required");
        return accountRepository.findByCompanyId(companyId, active).stream()
                .map(this::toResult)
                .toList();
    }

    private AccountResult toResult(Account account) {
        return new AccountResult(
                account.id(),
                account.companyId(),
                account.code(),
                account.name(),
                account.category(),
                account.level(),
                account.nature(),
                account.parentAccountId(),
                account.active());
    }

    private static void validate(CreateAccountCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.code(), "code is required");
        Objects.requireNonNull(command.name(), "name is required");
    }
}
