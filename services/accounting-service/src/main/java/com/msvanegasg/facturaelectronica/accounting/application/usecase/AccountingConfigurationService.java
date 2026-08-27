package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingConfigurationCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ConfigureAccountingUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;

public class AccountingConfigurationService implements ConfigureAccountingUseCase {

    private static final String CUSTOM_CONFIGURATION = "CUSTOM_ACCOUNTING_CONFIGURATION";

    private final AccountRepositoryPort accountRepository;
    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountingEntryRepositoryPort entryRepository;
    private final IdGeneratorPort idGenerator;

    public AccountingConfigurationService(AccountRepositoryPort accountRepository,
            AccountingRuleRepositoryPort ruleRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.entryRepository = Objects.requireNonNull(entryRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    @Transactional
    public AccountingSetupResult configure(AccountingConfigurationCommand command) {
        validate(command);
        List<Account> accounts = buildAccounts(command);
        Set<String> availableAccountCodes = availableAccountCodes(command, accounts);
        List<AccountingRule> rules = buildRules(command, availableAccountCodes);

        List<AccountResult> savedAccounts = accounts.stream()
                .map(accountRepository::save)
                .map(AccountingConfigurationService::toResult)
                .toList();
        List<AccountingRuleResult> savedRules = rules.stream()
                .map(rule -> {
                    ruleRepository.findActiveByCompanyIdAndEventType(rule.companyId(), rule.eventType())
                            .ifPresent(activeRule -> {
                                assertUnused(activeRule);
                                ruleRepository.save(activeRule.deactivate());
                            });
                    return ruleRepository.save(rule);
                })
                .map(this::toResult)
                .toList();
        return new AccountingSetupResult(command.companyId(), CUSTOM_CONFIGURATION, savedAccounts, savedRules);
    }

    private List<Account> buildAccounts(AccountingConfigurationCommand command) {
        Set<String> requestedCodes = new HashSet<>();
        return command.accounts().stream()
                .map(accountCommand -> {
                    validateAccount(accountCommand, command);
                    Account account = Account.create(idGenerator.newId(), command.companyId(), accountCommand.code(),
                            accountCommand.name(), accountCommand.parentAccountId());
                    if (!requestedCodes.add(account.code())) {
                        throw new IllegalStateException("duplicated account code in batch: " + account.code());
                    }
                    accountRepository.findByCompanyIdAndCode(command.companyId(), account.code())
                            .ifPresent(existing -> {
                                throw new IllegalStateException("account code already exists for company");
                            });
                    return account;
                })
                .toList();
    }

    private Set<String> availableAccountCodes(AccountingConfigurationCommand command, List<Account> accounts) {
        Set<String> accountCodes = new HashSet<>();
        accountRepository.findByCompanyId(command.companyId(), true).forEach(account -> accountCodes.add(account.code()));
        accounts.forEach(account -> accountCodes.add(account.code()));
        return accountCodes;
    }

    private List<AccountingRule> buildRules(AccountingConfigurationCommand command, Set<String> availableAccountCodes) {
        Set<String> eventTypes = new HashSet<>();
        return command.rules().stream()
                .map(ruleCommand -> {
                    validateRule(ruleCommand, command);
                    if (!eventTypes.add(ruleCommand.eventType().name())) {
                        throw new IllegalStateException("duplicated accounting rule event type in batch: "
                                + ruleCommand.eventType());
                    }
                    ruleCommand.lines().forEach(line -> assertAccountAvailable(line, availableAccountCodes));
                    assertRuleHasDebitAndCredit(ruleCommand);
                    return AccountingRule.create(idGenerator.newId(), command.companyId(), ruleCommand.eventType(),
                            ruleCommand.sourceType(), ruleCommand.name(), ruleCommand.lines().stream()
                                    .map(line -> AccountingRuleLine.create(line.accountCode(), line.side(),
                                            line.amountType(), line.description()))
                                    .toList());
                })
                .toList();
    }

    private static void validate(AccountingConfigurationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        if ((command.accounts() == null || command.accounts().isEmpty())
                && (command.rules() == null || command.rules().isEmpty())) {
            throw new IllegalArgumentException("accounting configuration requires accounts or rules");
        }
    }

    private static void validateAccount(CreateAccountCommand accountCommand, AccountingConfigurationCommand command) {
        Objects.requireNonNull(accountCommand, "account is required");
        if (!command.companyId().equals(accountCommand.companyId())) {
            throw new IllegalArgumentException("account companyId must match configuration companyId");
        }
        Objects.requireNonNull(accountCommand.code(), "account code is required");
        Objects.requireNonNull(accountCommand.name(), "account name is required");
    }

    private static void validateRule(CreateAccountingRuleCommand ruleCommand, AccountingConfigurationCommand command) {
        Objects.requireNonNull(ruleCommand, "accounting rule is required");
        if (!command.companyId().equals(ruleCommand.companyId())) {
            throw new IllegalArgumentException("accounting rule companyId must match configuration companyId");
        }
        Objects.requireNonNull(ruleCommand.eventType(), "eventType is required");
        Objects.requireNonNull(ruleCommand.sourceType(), "sourceType is required");
        Objects.requireNonNull(ruleCommand.name(), "name is required");
        if (ruleCommand.lines() == null || ruleCommand.lines().isEmpty()) {
            throw new IllegalArgumentException("accounting rule requires movements");
        }
    }

    private static void assertAccountAvailable(CreateAccountingRuleLineCommand line, Set<String> availableAccountCodes) {
        if (!availableAccountCodes.contains(line.accountCode())) {
            throw new IllegalStateException("account was not found: " + line.accountCode());
        }
    }

    private static void assertRuleHasDebitAndCredit(CreateAccountingRuleCommand command) {
        boolean hasDebit = command.lines().stream().anyMatch(line -> line.side() == AccountingEntrySide.DEBIT);
        boolean hasCredit = command.lines().stream().anyMatch(line -> line.side() == AccountingEntrySide.CREDIT);
        if (!hasDebit || !hasCredit) {
            throw new IllegalStateException("accounting rule requires debit and credit movements");
        }
    }

    private static AccountResult toResult(Account account) {
        return new AccountResult(account.id(), account.companyId(), account.code(), account.name(), account.category(),
                account.level(), account.nature(), account.parentAccountId(), account.active(), false, 0);
    }

    private AccountingRuleResult toResult(AccountingRule rule) {
        long usageCount = entryRepository.countByAccountingRuleId(rule.id());
        return new AccountingRuleResult(rule.id(), rule.companyId(), rule.eventType(), rule.sourceType(), rule.name(),
                rule.lines().stream()
                        .map(line -> new AccountingRuleLineResult(line.accountCode(), line.side(), line.amountType(),
                                line.description()))
                        .toList(),
                rule.active(),
                usageCount > 0,
                usageCount);
    }

    private void assertUnused(AccountingRule rule) {
        if (entryRepository.countByAccountingRuleId(rule.id()) > 0) {
            throw new IllegalStateException("used accounting rule cannot be replaced");
        }
    }
}
