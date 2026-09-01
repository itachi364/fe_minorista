package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateExpenseCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ExpenseResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ExpenseRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Expense;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

public class ExpenseManagementService implements ManageExpenseUseCase {

    private final ExpenseRepositoryPort expenseRepository;
    private final AccountsPayableRepositoryPort payableRepository;
    private final GenerateAccountingEntryUseCase accountingEntryUseCase;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public ExpenseManagementService(ExpenseRepositoryPort expenseRepository,
            AccountsPayableRepositoryPort payableRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock clock) {
        this.expenseRepository = Objects.requireNonNull(expenseRepository);
        this.payableRepository = Objects.requireNonNull(payableRepository);
        this.accountingEntryUseCase = Objects.requireNonNull(accountingEntryUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ExpenseResult create(CreateExpenseCommand command) {
        validateCreate(command);
        return expenseRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey())
                .map(AccountingOperationsMapper::toResult)
                .orElseGet(() -> createNew(command));
    }

    @Override
    public List<ExpenseResult> find(ExpenseQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("from cannot be after to");
        }
        return expenseRepository.find(query).stream().map(AccountingOperationsMapper::toResult).toList();
    }

    @Override
    public ExpenseResult confirm(UUID companyId, UUID expenseId) {
        Expense expense = expenseRepository.findByCompanyIdAndId(companyId, expenseId)
                .orElseThrow(() -> new IllegalStateException("expense was not found"));
        if (expense.status() == ExpenseStatus.CONFIRMED) {
            return AccountingOperationsMapper.toResult(expense);
        }
        Expense confirmed = expenseRepository.save(expense.confirm(clock.instant()));
        AccountingEventType eventType = confirmed.expenseType() == ExpenseType.ASSET_PURCHASE
                ? AccountingEventType.ASSET_PURCHASE_CONFIRMED
                : AccountingEventType.OPERATING_EXPENSE_CONFIRMED;
        accountingEntryUseCase.generate(new GenerateAccountingEntryCommand(confirmed.companyId(),
                eventType, AccountingSourceType.EXPENSE, confirmed.id(), confirmed.expenseDate(),
                confirmed.concept(), confirmed.supplierId(), confirmed.subtotal(), confirmed.taxTotal(),
                confirmed.total()));
        if (confirmed.paymentCondition() == PaymentCondition.CREDIT) {
            payableRepository.findByCompanyIdAndSource(confirmed.companyId(), AccountingSourceType.EXPENSE,
                    confirmed.id()).orElseGet(() -> payableRepository.save(AccountsPayable.open(idGenerator.newId(),
                            confirmed.companyId(), confirmed.supplierId(), AccountingSourceType.EXPENSE,
                            confirmed.id(), confirmed.expenseDate(), confirmed.dueDate(), confirmed.total(),
                            clock.instant())));
        }
        return AccountingOperationsMapper.toResult(confirmed);
    }

    private ExpenseResult createNew(CreateExpenseCommand command) {
        PaymentCondition paymentCondition = command.paymentCondition() == null ? PaymentCondition.CASH
                : command.paymentCondition();
        Expense expense = Expense.pending(idGenerator.newId(), command.companyId(), command.supplierId(),
                command.expenseType(), command.expenseDate(), command.concept(), command.subtotal(),
                command.taxTotal(), command.total(), paymentCondition, command.dueDate(), command.evidenceUrl(),
                command.idempotencyKey(), clock.instant());
        return AccountingOperationsMapper.toResult(expenseRepository.save(expense));
    }

    private static void validateCreate(CreateExpenseCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.expenseDate(), "expenseDate is required");
        Objects.requireNonNull(command.concept(), "concept is required");
        Objects.requireNonNull(command.subtotal(), "subtotal is required");
        Objects.requireNonNull(command.taxTotal(), "taxTotal is required");
        Objects.requireNonNull(command.total(), "total is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");
    }
}
