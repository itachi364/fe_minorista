package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.support.TransactionOperations;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountsPayableResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateFiscalDocumentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ConfirmPurchaseFiscalCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateAccountsPayableCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.GenerateAccountingEntryCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.PurchaseFiscalConfirmationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateFiscalDocumentUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ConfirmPurchaseFiscalUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.GenerateAccountingEntryUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageAccountsPayableUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalConfirmationProcessRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public class PurchaseFiscalConfirmationService implements ConfirmPurchaseFiscalUseCase {

    private static final AccountingSourceType SOURCE_TYPE = AccountingSourceType.PURCHASE;

    private final CalculateFiscalDocumentUseCase fiscalDocumentUseCase;
    private final GenerateAccountingEntryUseCase accountingEntryUseCase;
    private final ManageAccountsPayableUseCase accountsPayableUseCase;
    private final FiscalConfirmationProcessRepositoryPort processRepository;
    private final IdGeneratorPort idGenerator;
    private final TransactionOperations transactions;
    private final Clock clock;

    public PurchaseFiscalConfirmationService(CalculateFiscalDocumentUseCase fiscalDocumentUseCase,
            GenerateAccountingEntryUseCase accountingEntryUseCase,
            ManageAccountsPayableUseCase accountsPayableUseCase,
            FiscalConfirmationProcessRepositoryPort processRepository, IdGeneratorPort idGenerator,
            TransactionOperations transactions, Clock clock) {
        this.fiscalDocumentUseCase = Objects.requireNonNull(fiscalDocumentUseCase);
        this.accountingEntryUseCase = Objects.requireNonNull(accountingEntryUseCase);
        this.accountsPayableUseCase = Objects.requireNonNull(accountsPayableUseCase);
        this.processRepository = Objects.requireNonNull(processRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PurchaseFiscalConfirmationResult confirm(ConfirmPurchaseFiscalCommand command) {
        validate(command);
        UUID processId = idGenerator.newId();
        try {
            PurchaseFiscalConfirmationResult result = transactions.execute(status -> execute(command, processId));
            return Objects.requireNonNull(result, "purchase fiscal confirmation result is required");
        } catch (RuntimeException exception) {
            try {
                processRepository.markFailed(processId, command.companyId(), SOURCE_TYPE, command.sourceId(),
                        stableError(exception), clock.instant());
            } catch (RuntimeException persistenceFailure) {
                exception.addSuppressed(persistenceFailure);
            }
            throw exception;
        }
    }

    private PurchaseFiscalConfirmationResult execute(ConfirmPurchaseFiscalCommand command, UUID processId) {
        processRepository.markProcessing(processId, command.companyId(), SOURCE_TYPE, command.sourceId(),
                clock.instant());
        FiscalDocumentCalculationResult fiscal = fiscalDocumentUseCase.calculate(new CalculateFiscalDocumentCommand(
                command.companyId(), FiscalOperationType.PURCHASE, command.supplierId(), command.operationDate(),
                command.municipalityCode(), SOURCE_TYPE, command.sourceId(), command.lines(), command.contractId(),
                null));
        AccountingEntryResult entry = accountingEntryUseCase.generate(new GenerateAccountingEntryCommand(
                command.companyId(), AccountingEventType.PURCHASE_CONFIRMED, SOURCE_TYPE, command.sourceId(),
                command.operationDate(), "Factura de compra", command.supplierId(), command.subtotal(),
                command.taxTotal(), command.total(), amount(fiscal, WithholdingType.RETEFUENTE),
                amount(fiscal, WithholdingType.RETEIVA), amount(fiscal, WithholdingType.RETEICA),
                amount(fiscal, WithholdingType.AUTORETENCION), fiscal.withholdingTotal(), fiscal.netPayable()));
        AccountsPayableResult payable = command.creditPurchase()
                ? accountsPayableUseCase.create(new CreateAccountsPayableCommand(command.companyId(),
                        command.supplierId(), SOURCE_TYPE, command.sourceId(), command.operationDate(),
                        command.dueDate(), fiscal.netPayable()))
                : null;
        processRepository.markCompleted(command.companyId(), SOURCE_TYPE, command.sourceId(), fiscal.calculationId(),
                entry.id(), payable == null ? null : payable.id(), clock.instant());
        return new PurchaseFiscalConfirmationResult(fiscal, entry, payable, "COMPLETED");
    }

    private static BigDecimal amount(FiscalDocumentCalculationResult fiscal, WithholdingType type) {
        return fiscal.lines().stream().flatMap(line -> line.items().stream())
                .filter(item -> item.withholdingType() == type && item.decision() == WithholdingDecision.APPLIED)
                .map(item -> item.amount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void validate(ConfirmPurchaseFiscalCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.sourceId(), "sourceId is required");
        Objects.requireNonNull(command.supplierId(), "supplierId is required");
        Objects.requireNonNull(command.operationDate(), "operationDate is required");
        Objects.requireNonNull(command.subtotal(), "subtotal is required");
        Objects.requireNonNull(command.taxTotal(), "taxTotal is required");
        Objects.requireNonNull(command.total(), "total is required");
        if (command.lines().isEmpty()) throw new IllegalArgumentException("lines are required");
        if (command.creditPurchase() && command.dueDate() == null) {
            throw new IllegalArgumentException("dueDate is required for credit purchases");
        }
        if (command.subtotal().add(command.taxTotal()).compareTo(command.total()) != 0) {
            throw new IllegalArgumentException("subtotal plus taxTotal must equal total");
        }
    }

    private static String stableError(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) return exception.getClass().getSimpleName();
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
