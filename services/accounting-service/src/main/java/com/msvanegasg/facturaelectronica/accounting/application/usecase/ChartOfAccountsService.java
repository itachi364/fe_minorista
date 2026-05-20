package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.Objects;
import java.util.UUID;

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
    public AccountResult findByCode(UUID companyId, String code) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(code, "code is required");
        return accountRepository.findByCompanyIdAndCode(companyId, code.trim())
                .map(this::toResult)
                .orElseThrow(() -> new IllegalStateException("account was not found"));
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
