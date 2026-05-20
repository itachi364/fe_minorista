package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;

public class GenerateAccountingEntryService implements GenerateAccountingEntryUseCase {

    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountRepositoryPort accountRepository;
    private final AccountingEntryRepositoryPort entryRepository;
    private final IdGeneratorPort idGenerator;

    public GenerateAccountingEntryService(
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
    public AccountingEntryResult generate(GenerateAccountingEntryCommand command) {
        validate(command);
        if (entryRepository.existsByCompanyIdAndSource(command.companyId(), command.sourceType(), command.sourceId())) {
            return entryRepository.findByCompanyIdAndSource(command.companyId(), command.sourceType(),
                    command.sourceId()).map(this::toResult)
                    .orElseThrow(() -> new IllegalStateException("source document is already posted"));
        }

        AccountingRule rule = ruleRepository.findActiveByCompanyIdAndEventType(command.companyId(), command.eventType())
                .orElseThrow(() -> new IllegalStateException("accounting rule was not found"));
        if (rule.sourceType() != command.sourceType()) {
            throw new IllegalStateException("accounting rule source type does not match command source type");
        }

        List<AccountingEntryLine> lines = rule.lines().stream()
                .map(ruleLine -> toEntryLine(command, ruleLine))
                .filter(Objects::nonNull)
                .toList();

        AccountingEntry entry = AccountingEntry.post(
                idGenerator.newId(),
                command.companyId(),
                command.entryDate(),
                command.description(),
                command.sourceType(),
                command.sourceId(),
                lines);

        return toResult(entryRepository.save(entry));
    }

    private AccountingEntryLine toEntryLine(GenerateAccountingEntryCommand command, AccountingRuleLine ruleLine) {
        BigDecimal amount = amountOf(command, ruleLine.amountType());
        if (amount.signum() == 0) {
            return null;
        }

        Account account = accountRepository.findByCompanyIdAndCode(command.companyId(), ruleLine.accountCode())
                .orElseThrow(() -> new IllegalStateException("account was not found: " + ruleLine.accountCode()));
        if (!account.active()) {
            throw new IllegalStateException("account is inactive: " + ruleLine.accountCode());
        }

        BigDecimal debit = ruleLine.side() == AccountingEntrySide.DEBIT ? amount : BigDecimal.ZERO;
        BigDecimal credit = ruleLine.side() == AccountingEntrySide.CREDIT ? amount : BigDecimal.ZERO;
        return AccountingEntryLine.create(
                idGenerator.newId(),
                account.id(),
                account.code(),
                account.name(),
                command.thirdpartyId(),
                debit,
                credit,
                ruleLine.description());
    }

    private static BigDecimal amountOf(GenerateAccountingEntryCommand command, AccountingAmountType amountType) {
        BigDecimal amount = switch (amountType) {
            case SUBTOTAL -> command.subtotal();
            case TAX_TOTAL -> command.taxTotal();
            case TOTAL -> command.total();
        };
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("accounting amount cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private AccountingEntryResult toResult(AccountingEntry entry) {
        return new AccountingEntryResult(
                entry.id(),
                entry.companyId(),
                entry.entryDate(),
                entry.description(),
                entry.sourceType(),
                entry.sourceId(),
                entry.status(),
                entry.debitTotal(),
                entry.creditTotal(),
                entry.lines().stream().map(this::toLineResult).toList());
    }

    private AccountingEntryLineResult toLineResult(AccountingEntryLine line) {
        return new AccountingEntryLineResult(
                line.id(),
                line.accountId(),
                line.accountCode(),
                line.accountName(),
                line.thirdpartyId(),
                line.debitAmount(),
                line.creditAmount(),
                line.description());
    }

    private static void validate(GenerateAccountingEntryCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.eventType(), "eventType is required");
        Objects.requireNonNull(command.sourceType(), "sourceType is required");
        Objects.requireNonNull(command.sourceId(), "sourceId is required");
        Objects.requireNonNull(command.entryDate(), "entryDate is required");
        Objects.requireNonNull(command.subtotal(), "subtotal is required");
        Objects.requireNonNull(command.taxTotal(), "taxTotal is required");
        Objects.requireNonNull(command.total(), "total is required");
        if (command.description() == null || command.description().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
    }
}
