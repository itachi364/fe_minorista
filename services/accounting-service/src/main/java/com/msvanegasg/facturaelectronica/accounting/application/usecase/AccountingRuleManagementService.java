package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingRuleResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountingRuleLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountingRulesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;

public class AccountingRuleManagementService implements ManageAccountingRulesUseCase {

    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountRepositoryPort accountRepository;
    private final IdGeneratorPort idGenerator;

    public AccountingRuleManagementService(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            IdGeneratorPort idGenerator) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public AccountingRuleResult create(CreateAccountingRuleCommand command) {
        validate(command);
        ruleRepository.findActiveByCompanyIdAndEventType(command.companyId(), command.eventType())
                .ifPresent(rule -> {
                    throw new IllegalStateException("active accounting rule already exists for event type");
                });
        return createActiveRule(command);
    }

    @Override
    public AccountingRuleResult replaceActive(CreateAccountingRuleCommand command) {
        validate(command);
        ruleRepository.findActiveByCompanyIdAndEventType(command.companyId(), command.eventType())
                .ifPresent(rule -> ruleRepository.save(rule.deactivate()));
        return createActiveRule(command);
    }

    @Override
    public List<AccountingRuleResult> replaceActiveAll(List<CreateAccountingRuleCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("accounting rules batch requires at least one rule");
        }
        Set<String> eventTypes = new HashSet<>();
        commands.forEach(command -> {
            validate(command);
            String key = command.companyId() + ":" + command.eventType();
            if (!eventTypes.add(key)) {
                throw new IllegalStateException("duplicated accounting rule event type in batch: "
                        + command.eventType());
            }
            command.lines().forEach(line -> assertAccountExists(command, line));
            assertRuleHasDebitAndCredit(command);
        });
        return commands.stream()
                .map(this::replaceActive)
                .toList();
    }

    @Override
    public AccountingRuleResult deactivateActive(UUID companyId, AccountingEventType eventType) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        AccountingRule activeRule = ruleRepository.findActiveByCompanyIdAndEventType(companyId, eventType)
                .orElseThrow(() -> new IllegalStateException("active accounting rule was not found"));
        return toResult(ruleRepository.save(activeRule.deactivate()));
    }

    @Override
    public List<AccountingRuleResult> find(UUID companyId, AccountingEventType eventType, Boolean active) {
        Objects.requireNonNull(companyId, "companyId is required");
        return ruleRepository.findByCompanyId(companyId, eventType, active).stream()
                .map(AccountingRuleManagementService::toResult)
                .toList();
    }

    private AccountingRuleResult createActiveRule(CreateAccountingRuleCommand command) {
        command.lines().forEach(line -> assertAccountExists(command, line));
        AccountingRule rule = AccountingRule.create(
                idGenerator.newId(),
                command.companyId(),
                command.eventType(),
                command.sourceType(),
                command.name(),
                command.lines().stream()
                        .map(line -> AccountingRuleLine.create(
                                line.accountCode(),
                                line.side(),
                                line.amountType(),
                                line.description()))
                        .toList());

        return toResult(ruleRepository.save(rule));
    }

    private void assertAccountExists(CreateAccountingRuleCommand command, CreateAccountingRuleLineCommand line) {
        accountRepository.findByCompanyIdAndCode(command.companyId(), line.accountCode())
                .orElseThrow(() -> new IllegalStateException("account was not found: " + line.accountCode()));
    }

    private static void assertRuleHasDebitAndCredit(CreateAccountingRuleCommand command) {
        boolean hasDebit = command.lines().stream().anyMatch(line -> line.side() == AccountingEntrySide.DEBIT);
        boolean hasCredit = command.lines().stream().anyMatch(line -> line.side() == AccountingEntrySide.CREDIT);
        if (!hasDebit || !hasCredit) {
            throw new IllegalStateException("accounting rule requires debit and credit movements");
        }
    }

    private static AccountingRuleResult toResult(AccountingRule rule) {
        return new AccountingRuleResult(
                rule.id(),
                rule.companyId(),
                rule.eventType(),
                rule.sourceType(),
                rule.name(),
                rule.lines().stream()
                        .map(line -> new AccountingRuleLineResult(
                                line.accountCode(),
                                line.side(),
                                line.amountType(),
                                line.description()))
                        .toList(),
                rule.active());
    }

    private static void validate(CreateAccountingRuleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.eventType(), "eventType is required");
        Objects.requireNonNull(command.sourceType(), "sourceType is required");
        Objects.requireNonNull(command.name(), "name is required");
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new IllegalArgumentException("accounting rule requires lines");
        }
    }
}
