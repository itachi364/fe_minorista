package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CudeGenerator;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;

public class SaleManagementService implements ManageSaleUseCase {

    private final SaleRepositoryPort saleRepository;
    private final InventoryAvailabilityPort inventoryAvailability;
    private final ElectronicDocumentProviderPort providerPort;
    private final InventoryMovementPort inventoryMovementPort;
    private final AccountingEntryPort accountingEntryPort;
    private final AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.inventoryAvailability = Objects.requireNonNull(inventoryAvailability);
        this.providerPort = Objects.requireNonNull(providerPort);
        this.inventoryMovementPort = Objects.requireNonNull(inventoryMovementPort);
        this.accountingEntryPort = Objects.requireNonNull(accountingEntryPort);
        this.assignFiscalNumberUseCase = Objects.requireNonNull(assignFiscalNumberUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public SaleResult create(CreateSaleCommand command) {
        validate(command);
        return saleRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey())
                .map(BillingResultMapper::toSaleResult)
                .orElseGet(() -> createNew(command));
    }

    @Override
    public SaleResult confirm(UUID companyId, UUID saleId, String idempotencyKey) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(saleId, "saleId is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Sale sale = saleRepository.findByCompanyIdAndId(companyId, saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        if (sale.status() != SaleStatus.DRAFT) {
            return BillingResultMapper.toSaleResult(applyPostValidationEffects(sale));
        }
        sale.lines().forEach(line -> ensureAvailable(sale.companyId(), line.productId(), line.quantity()));
        UUID documentId = idGenerator.newId();
        Instant now = clock.now();
        ElectronicDocumentType documentType = sale.saleChannel() == SaleChannel.POS
                ? ElectronicDocumentType.ELECTRONIC_POS
                : ElectronicDocumentType.ELECTRONIC_INVOICE;
        FiscalNumberResult fiscalNumber = assignFiscalNumberUseCase.assign(new AssignFiscalNumberCommand(
                sale.companyId(), documentType, LocalDate.ofInstant(now, ZoneOffset.UTC), FiscalEnvironment.TEST));
        ProviderSubmissionResult provider = providerPort.submitElectronicPos(sale, documentId, idempotencyKey);
        ElectronicDocument document = documentFromProvider(documentId, sale, documentType, fiscalNumber, provider,
                idempotencyKey, now);
        Sale confirmed = saleRepository.save(sale.confirm(document, now));
        return BillingResultMapper.toSaleResult(applyPostValidationEffects(confirmed));
    }

    @Override
    public SaleResult findById(UUID companyId, UUID saleId) {
        return BillingResultMapper.toSaleResult(saleRepository.findByCompanyIdAndId(companyId, saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId)));
    }

    private SaleResult createNew(CreateSaleCommand command) {
        var lines = command.lines().stream().map(this::toLine).toList();
        lines.forEach(line -> ensureAvailable(command.companyId(), line.productId(), line.quantity()));
        Sale sale = Sale.draft(idGenerator.newId(), command.companyId(), command.customerId(), command.paymentMethodId(),
                command.saleChannel() == null ? SaleChannel.POS : command.saleChannel(), command.idempotencyKey(),
                command.createdBy(), clock.now(), lines);
        return BillingResultMapper.toSaleResult(saleRepository.save(sale));
    }

    private SaleLine toLine(SaleLineCommand command) {
        return SaleLine.calculate(idGenerator.newId(), command.productId(), command.quantity(), command.unitPrice(),
                command.discountAmount(), command.taxCode(), command.taxRate());
    }

    private void ensureAvailable(UUID companyId, UUID productId, BigDecimal quantity) {
        if (!inventoryAvailability.isAvailable(companyId, productId, quantity)) {
            throw new InsufficientStockException(productId);
        }
    }

    private ElectronicDocument documentFromProvider(UUID documentId, Sale sale, ElectronicDocumentType documentType,
            FiscalNumberResult fiscalNumber, ProviderSubmissionResult provider, String idempotencyKey,
            Instant issuedAt) {
        ProviderStatus providerStatus = provider.status();
        ElectronicDocumentStatus status = switch (providerStatus) {
            case ACCEPTED -> ElectronicDocumentStatus.VALIDATED;
            case REJECTED -> ElectronicDocumentStatus.REJECTED;
            case FAILED -> ElectronicDocumentStatus.FAILED;
        };
        String cude = provider.cufeCude() == null || provider.cufeCude().isBlank()
                ? CudeGenerator.generate(
                        sale.companyId() + "|" + sale.id() + "|" + fiscalNumber.number() + "|" + sale.total())
                : provider.cufeCude();
        String qr = provider.qrContent() == null || provider.qrContent().isBlank()
                ? "mock-qr:" + cude
                : provider.qrContent();
        return new ElectronicDocument(documentId, sale.companyId(), sale.id(), documentType, status, providerStatus,
                fiscalNumber.prefix(), fiscalNumber.number(), cude, qr, sale.subtotal(), sale.taxTotal(),
                sale.total(), provider.trackingId(), provider.errorCode(),
                provider.errorMessage(), idempotencyKey, issuedAt, null, null);
    }

    private Sale applyPostValidationEffects(Sale sale) {
        ElectronicDocument document = sale.electronicDocument();
        if (document == null || document.status() != ElectronicDocumentStatus.VALIDATED) {
            return sale;
        }
        Sale current = sale;
        ElectronicDocument currentDocument = document;
        String effectIdempotencyKey = currentDocument.idempotencyKey();
        if (!currentDocument.inventoryApplied()) {
            inventoryMovementPort.applySaleOut(current, effectIdempotencyKey);
            currentDocument = currentDocument.markInventoryApplied(clock.now());
            current = saleRepository.save(current.withElectronicDocument(currentDocument));
        }
        if (!currentDocument.accountingApplied()) {
            accountingEntryPort.postSale(current, effectIdempotencyKey);
            currentDocument = currentDocument.markAccountingApplied(clock.now());
            current = saleRepository.save(current.withElectronicDocument(currentDocument));
        }
        return current;
    }

    private static void validate(CreateSaleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");
        Objects.requireNonNull(command.lines(), "lines are required");
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
    }
}
