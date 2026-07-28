package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsReceivableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsReceivableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterReceivablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsReceivableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivablePayment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;

public class AccountsReceivableManagementService implements ManageAccountsReceivableUseCase {

    private final AccountsReceivableRepositoryPort receivableRepository;
    private final AccountsReceivablePaymentRepositoryPort paymentRepository;
    private final GenerateAccountingEntryUseCase accountingEntryUseCase;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public AccountsReceivableManagementService(AccountsReceivableRepositoryPort receivableRepository,
            AccountsReceivablePaymentRepositoryPort paymentRepository,
            GenerateAccountingEntryUseCase accountingEntryUseCase, IdGeneratorPort idGenerator, Clock clock) {
        this.receivableRepository = Objects.requireNonNull(receivableRepository);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.accountingEntryUseCase = Objects.requireNonNull(accountingEntryUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public AccountsReceivableResult create(CreateAccountsReceivableCommand command) {
        validateCreate(command);
        return receivableRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey())
                .or(() -> receivableRepository.findByCompanyIdAndSource(command.companyId(), command.sourceType(),
                        command.sourceId()))
                .map(AccountingOperationsMapper::toResult)
                .orElseGet(() -> AccountingOperationsMapper.toResult(receivableRepository.save(AccountsReceivable.open(
                        idGenerator.newId(), command.companyId(), command.customerId(), command.sourceType(),
                        command.sourceId(), command.issueDate(), command.dueDate(), command.totalAmount(),
                        command.idempotencyKey(), clock.instant()))));
    }

    @Override
    public List<AccountsReceivableResult> find(UUID companyId, AccountsReceivableStatus status, UUID customerId,
            LocalDate from, LocalDate to) {
        Objects.requireNonNull(companyId, "companyId is required");
        return receivableRepository.find(companyId, status, customerId, from, to).stream()
                .map(AccountingOperationsMapper::toResult)
                .toList();
    }

    @Override
    public AccountsReceivablePaymentResult registerPayment(RegisterReceivablePaymentCommand command) {
        validatePayment(command);
        AccountsReceivable receivable = receivableRepository.findByCompanyIdAndId(command.companyId(),
                command.receivableId()).orElseThrow(() -> new IllegalStateException("accounts receivable was not found"));
        AccountsReceivable updated = receivable.applyPayment(command.amount());
        AccountsReceivablePayment payment = new AccountsReceivablePayment(idGenerator.newId(), command.companyId(),
                command.receivableId(), command.paymentDate(), command.amount(), command.paymentMethod(),
                command.reference(), command.createdBy(), clock.instant());
        AccountsReceivable savedReceivable = receivableRepository.save(updated);
        AccountsReceivablePayment savedPayment = paymentRepository.save(payment);
        accountingEntryUseCase.generate(new GenerateAccountingEntryCommand(command.companyId(),
                AccountingEventType.ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED,
                AccountingSourceType.ACCOUNTS_RECEIVABLE_PAYMENT, savedPayment.id(), command.paymentDate(),
                "Recaudo cuenta por cobrar", receivable.customerId(), command.amount(), BigDecimal.ZERO,
                command.amount()));
        return AccountingOperationsMapper.toResult(savedPayment, savedReceivable);
    }

    private static void validatePayment(RegisterReceivablePaymentCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.receivableId(), "receivableId is required");
        Objects.requireNonNull(command.paymentDate(), "paymentDate is required");
        Objects.requireNonNull(command.amount(), "amount is required");
        Objects.requireNonNull(command.paymentMethod(), "paymentMethod is required");
    }

    private static void validateCreate(CreateAccountsReceivableCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.customerId(), "customerId is required");
        Objects.requireNonNull(command.sourceType(), "sourceType is required");
        Objects.requireNonNull(command.sourceId(), "sourceId is required");
        Objects.requireNonNull(command.issueDate(), "issueDate is required");
        Objects.requireNonNull(command.dueDate(), "dueDate is required");
        Objects.requireNonNull(command.totalAmount(), "totalAmount is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");
    }
}