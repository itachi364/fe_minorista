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
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;

public class AccountingRuleManagementService implements ManageAccountingRulesUseCase {

    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountRepositoryPort accountRepository;
    private final AccountingEntryRepositoryPort entryRepository;
    private final IdGeneratorPort idGenerator;

    public AccountingRuleManagementService(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.entryRepository = Objects.requireNonNull(entryRepository);
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
                .ifPresent(rule -> {
                    assertUnused(rule.id(), "used accounting rule cannot be replaced");
                    ruleRepository.save(rule.deactivate());
                });
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
    public AccountingRuleResult update(UUID companyId, UUID ruleId, CreateAccountingRuleCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(ruleId, "ruleId is required");
        validate(command);
        if (!companyId.equals(command.companyId())) {
            throw new IllegalArgumentException("accounting rule companyId must match path companyId");
        }
        AccountingRule existing = ruleRepository.findByCompanyIdAndId(companyId, ruleId)
                .orElseThrow(() -> new IllegalStateException("accounting rule was not found"));
        assertUnused(existing.id(), "used accounting rule cannot be updated");
        ruleRepository.findActiveByCompanyIdAndEventType(companyId, command.eventType())
                .filter(activeRule -> !activeRule.id().equals(ruleId))
                .ifPresent(activeRule -> {
                    throw new IllegalStateException("active accounting rule already exists for event type");
                });
        command.lines().forEach(line -> assertAccountExists(command, line));
        assertRuleHasDebitAndCredit(command);
        AccountingRule updated = existing.update(command.eventType(), command.sourceType(), command.name(),
                command.lines().stream()
                        .map(line -> AccountingRuleLine.create(line.accountCode(), line.side(), line.amountType(),
                                line.description()))
                        .toList());
        return toResult(ruleRepository.save(updated));
    }

    @Override
    public AccountingRuleResult deactivate(UUID companyId, UUID ruleId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(ruleId, "ruleId is required");
        AccountingRule existing = ruleRepository.findByCompanyIdAndId(companyId, ruleId)
                .orElseThrow(() -> new IllegalStateException("accounting rule was not found"));
        assertUnused(existing.id(), "used accounting rule cannot be deactivated");
        return toResult(ruleRepository.save(existing.deactivate()));
    }

    @Override
    public AccountingRuleResult deactivateActive(UUID companyId, AccountingEventType eventType) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        AccountingRule activeRule = ruleRepository.findActiveByCompanyIdAndEventType(companyId, eventType)
                .orElseThrow(() -> new IllegalStateException("active accounting rule was not found"));
        assertUnused(activeRule.id(), "used accounting rule cannot be deactivated");
        return toResult(ruleRepository.save(activeRule.deactivate()));
    }

    @Override
    public List<AccountingRuleResult> find(UUID companyId, AccountingEventType eventType, Boolean active) {
        Objects.requireNonNull(companyId, "companyId is required");
        return ruleRepository.findByCompanyId(companyId, eventType, active).stream()
                .map(this::toResult)
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

    private AccountingRuleResult toResult(AccountingRule rule) {
        long usageCount = entryRepository.countByAccountingRuleId(rule.id());
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
                rule.active(),
                usageCount > 0,
                usageCount);
    }

    private void assertUnused(UUID ruleId, String message) {
        if (entryRepository.countByAccountingRuleId(ruleId) > 0) {
            throw new IllegalStateException(message);
        }
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
