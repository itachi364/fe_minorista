package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class GenerateAccountingEntryService implements GenerateAccountingEntryUseCase {

    private final AccountingRuleRepositoryPort ruleRepository;
    private final AccountRepositoryPort accountRepository;
    private final AccountingEntryRepositoryPort entryRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public GenerateAccountingEntryService(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            IdGeneratorPort idGenerator) {
        this(ruleRepository, accountRepository, entryRepository, DomainEventPublisherPort.noop(), idGenerator,
                Clock.systemUTC());
    }

    public GenerateAccountingEntryService(
            AccountingRuleRepositoryPort ruleRepository,
            AccountRepositoryPort accountRepository,
            AccountingEntryRepositoryPort entryRepository,
            DomainEventPublisherPort eventPublisher,
            IdGeneratorPort idGenerator,
            Clock clock) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.entryRepository = Objects.requireNonNull(entryRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
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
                .orElseThrow(() -> new IllegalStateException("accounting rule was not found: " + command.eventType()));
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
                rule.id(),
                lines);

        AccountingEntry saved = entryRepository.save(entry);
        publishAccountingEntryPosted(saved);
        return toResult(saved);
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

    private void publishAccountingEntryPosted(AccountingEntry entry) {
        eventPublisher.publish(new DomainEventEnvelope(idGenerator.newId(), EventTypes.ACCOUNTING_ENTRY_POSTED, 1,
                clock.instant(), entry.companyId(), "AccountingEntry", entry.id(), "accounting-service", null,
                entry.sourceType().name() + ":" + entry.sourceId() + ":accounting-entry-posted", entryPayload(entry)));
    }

    private static Map<String, Object> entryPayload(AccountingEntry entry) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("entryId", entry.id().toString());
        payload.put("entryDate", entry.entryDate().toString());
        payload.put("description", entry.description());
        payload.put("sourceType", entry.sourceType().name());
        payload.put("sourceId", entry.sourceId().toString());
        payload.put("status", entry.status().name());
        payload.put("debitTotal", entry.debitTotal());
        payload.put("creditTotal", entry.creditTotal());
        return payload;
    }
    private AccountingEntryResult toResult(AccountingEntry entry) {
        return new AccountingEntryResult(
                entry.id(),
                entry.companyId(),
                entry.entryDate(),
                entry.description(),
                entry.sourceType(),
                entry.sourceId(),
                entry.accountingRuleId(),
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
