package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayablePaymentResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsPayableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.RegisterPayablePaymentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayablePaymentRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayablePayment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;

public class AccountsPayableManagementService implements ManageAccountsPayableUseCase {

    private final AccountsPayableRepositoryPort payableRepository;
    private final AccountsPayablePaymentRepositoryPort paymentRepository;
    private final GenerateAccountingEntryUseCase accountingEntryUseCase;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public AccountsPayableManagementService(AccountsPayableRepositoryPort payableRepository,
            AccountsPayablePaymentRepositoryPort paymentRepository, GenerateAccountingEntryUseCase accountingEntryUseCase,
            IdGeneratorPort idGenerator, Clock clock) {
        this.payableRepository = Objects.requireNonNull(payableRepository);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.accountingEntryUseCase = Objects.requireNonNull(accountingEntryUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public AccountsPayableResult create(CreateAccountsPayableCommand command) {
        validateCreate(command);
        return payableRepository.findByCompanyIdAndSource(command.companyId(), command.sourceType(),
                command.sourceId())
                .map(AccountingOperationsMapper::toResult)
                .orElseGet(() -> AccountingOperationsMapper.toResult(payableRepository.save(AccountsPayable.open(
                        idGenerator.newId(), command.companyId(), command.supplierId(), command.sourceType(),
                        command.sourceId(), command.issueDate(), command.dueDate(), command.totalAmount(),
                        clock.instant()))));
    }

    @Override
    public List<AccountsPayableResult> find(UUID companyId, AccountsPayableStatus status, UUID supplierId,
            LocalDate from, LocalDate to) {
        Objects.requireNonNull(companyId, "companyId is required");
        return payableRepository.find(companyId, status, supplierId, from, to).stream()
                .map(AccountingOperationsMapper::toResult)
                .toList();
    }

    @Override
    public AccountsPayablePaymentResult registerPayment(RegisterPayablePaymentCommand command) {
        validatePayment(command);
        AccountsPayable payable = payableRepository.findByCompanyIdAndId(command.companyId(), command.payableId())
                .orElseThrow(() -> new IllegalStateException("accounts payable was not found"));
        AccountsPayable updated = payable.applyPayment(command.amount());
        AccountsPayablePayment payment = new AccountsPayablePayment(idGenerator.newId(), command.companyId(),
                command.payableId(), command.paymentDate(), command.amount(), command.paymentMethod(),
                command.reference(), command.createdBy(), clock.instant());
        AccountsPayable savedPayable = payableRepository.save(updated);
        AccountsPayablePayment savedPayment = paymentRepository.save(payment);
        accountingEntryUseCase.generate(new GenerateAccountingEntryCommand(command.companyId(),
                AccountingEventType.ACCOUNTS_PAYABLE_PAYMENT_REGISTERED,
                AccountingSourceType.ACCOUNTS_PAYABLE_PAYMENT, savedPayment.id(), command.paymentDate(),
                "Pago cuenta por pagar", payable.supplierId(), command.amount(), BigDecimal.ZERO, command.amount()));
        return AccountingOperationsMapper.toResult(savedPayment, savedPayable);
    }

    private static void validatePayment(RegisterPayablePaymentCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.payableId(), "payableId is required");
        Objects.requireNonNull(command.paymentDate(), "paymentDate is required");
        Objects.requireNonNull(command.amount(), "amount is required");
        Objects.requireNonNull(command.paymentMethod(), "paymentMethod is required");
    }

    private static void validateCreate(CreateAccountsPayableCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.sourceType(), "sourceType is required");
        Objects.requireNonNull(command.sourceId(), "sourceId is required");
        Objects.requireNonNull(command.issueDate(), "issueDate is required");
        Objects.requireNonNull(command.dueDate(), "dueDate is required");
        Objects.requireNonNull(command.totalAmount(), "totalAmount is required");
    }
}
