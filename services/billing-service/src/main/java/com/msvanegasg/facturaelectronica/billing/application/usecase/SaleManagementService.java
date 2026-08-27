package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.AuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.AuditResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalArtifactResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.InventoryProductSnapshot;
import com.msvanegasg.facturaelectronica.billing.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.PosReceiptResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleDocumentTypeOverrideCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AuditEventPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.CompanyFiscalPolicyRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FinalConsumerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalDocumentUsagePort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.OperationalPinValidationPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleDocumentTypeOverrideRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CudeGenerator;
import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerIdentificationMode;
import com.msvanegasg.facturaelectronica.billing.domain.model.CompanyFiscalPolicy;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleDocumentTypeOverride;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class SaleManagementService implements ManageSaleUseCase {

    private final SaleRepositoryPort saleRepository;
    private final InventoryAvailabilityPort inventoryAvailability;
    private final ElectronicDocumentProviderPort providerPort;
    private final InventoryMovementPort inventoryMovementPort;
    private final AccountingEntryPort accountingEntryPort;
    private final AuditEventPort auditEventPort;
    private final FinalConsumerProfileRepositoryPort finalConsumerProfileRepository;
    private final LicenseValidationPort licenseValidationPort;
    private final FiscalDocumentUsagePort fiscalDocumentUsagePort;
    private final CompanyFiscalPolicyRepositoryPort companyFiscalPolicyRepository;
    private final SaleDocumentTypeOverrideRepositoryPort saleDocumentTypeOverrideRepository;
    private final OperationalPinValidationPort operationalPinValidationPort;
    private final AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    private final DomainEventPublisherPort eventPublisher;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AuditEventPort auditEventPort,
            LicenseValidationPort licenseValidationPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort, accountingEntryPort,
                auditEventPort, companyId -> java.util.Optional.of(new com.msvanegasg.facturaelectronica.billing.domain.model.FinalConsumerProfile(
                        new UUID(0L, 222L), null, "FINAL_CONSUMER", 31, "222222222222", "Consumidor final",
                true, "TEST", "TEST", Instant.EPOCH)),
                licenseValidationPort, assignFiscalNumberUseCase, DomainEventPublisherPort.noop(), idGenerator,
                clock);
    }

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AuditEventPort auditEventPort,
            FinalConsumerProfileRepositoryPort finalConsumerProfileRepository,
            LicenseValidationPort licenseValidationPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort, accountingEntryPort,
                auditEventPort, finalConsumerProfileRepository, licenseValidationPort, assignFiscalNumberUseCase,
                DomainEventPublisherPort.noop(), idGenerator, clock);
    }

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AuditEventPort auditEventPort,
            LicenseValidationPort licenseValidationPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            DomainEventPublisherPort eventPublisher, IdGeneratorPort idGenerator, ClockPort clock) {
        this(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort, accountingEntryPort,
                auditEventPort, companyId -> java.util.Optional.of(new com.msvanegasg.facturaelectronica.billing.domain.model.FinalConsumerProfile(
                        new UUID(0L, 222L), null, "FINAL_CONSUMER", 31, "222222222222", "Consumidor final",
                true, "TEST", "TEST", Instant.EPOCH)),
                licenseValidationPort, assignFiscalNumberUseCase, eventPublisher, idGenerator, clock);
    }

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AuditEventPort auditEventPort,
            FinalConsumerProfileRepositoryPort finalConsumerProfileRepository,
            LicenseValidationPort licenseValidationPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            DomainEventPublisherPort eventPublisher, IdGeneratorPort idGenerator, ClockPort clock) {
        this(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort, accountingEntryPort,
                auditEventPort, finalConsumerProfileRepository, licenseValidationPort, FiscalDocumentUsagePort.noop(),
                CompanyFiscalPolicyRepositoryPort.defaultsOnly(), SaleDocumentTypeOverrideRepositoryPort.noop(),
                OperationalPinValidationPort.allowAll(), assignFiscalNumberUseCase, eventPublisher, idGenerator,
                clock);
    }

    public SaleManagementService(SaleRepositoryPort saleRepository, InventoryAvailabilityPort inventoryAvailability,
            ElectronicDocumentProviderPort providerPort, InventoryMovementPort inventoryMovementPort,
            AccountingEntryPort accountingEntryPort, AuditEventPort auditEventPort,
            FinalConsumerProfileRepositoryPort finalConsumerProfileRepository,
            LicenseValidationPort licenseValidationPort, FiscalDocumentUsagePort fiscalDocumentUsagePort,
            CompanyFiscalPolicyRepositoryPort companyFiscalPolicyRepository,
            SaleDocumentTypeOverrideRepositoryPort saleDocumentTypeOverrideRepository,
            OperationalPinValidationPort operationalPinValidationPort,
            AssignFiscalNumberUseCase assignFiscalNumberUseCase, DomainEventPublisherPort eventPublisher,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.inventoryAvailability = Objects.requireNonNull(inventoryAvailability);
        this.providerPort = Objects.requireNonNull(providerPort);
        this.inventoryMovementPort = Objects.requireNonNull(inventoryMovementPort);
        this.accountingEntryPort = Objects.requireNonNull(accountingEntryPort);
        this.auditEventPort = Objects.requireNonNull(auditEventPort);
        this.finalConsumerProfileRepository = Objects.requireNonNull(finalConsumerProfileRepository);
        this.licenseValidationPort = Objects.requireNonNull(licenseValidationPort);
        this.fiscalDocumentUsagePort = Objects.requireNonNull(fiscalDocumentUsagePort);
        this.companyFiscalPolicyRepository = Objects.requireNonNull(companyFiscalPolicyRepository);
        this.saleDocumentTypeOverrideRepository = Objects.requireNonNull(saleDocumentTypeOverrideRepository);
        this.operationalPinValidationPort = Objects.requireNonNull(operationalPinValidationPort);
        this.assignFiscalNumberUseCase = Objects.requireNonNull(assignFiscalNumberUseCase);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
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
    public SaleResult close(CreateSaleCommand command) {
        SaleResult sale = create(command);
        return confirm(command.companyId(), sale.id(), command.idempotencyKey());
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
        LicensePolicy licensePolicy = licenseValidationPort.policy(companyId, LicenseAction.ISSUE_FISCAL_DOCUMENT);
        sale.lines().forEach(line -> ensureAvailable(sale.companyId(), line));
        Instant now = clock.now();
        ensureMonthlyDocumentQuota(companyId, licensePolicy, now);
        ElectronicDocumentType documentType = resolveSaleDocumentType(sale);
        accountingEntryPort.ensureSalePostingConfigured(companyId);
        UUID documentId = idGenerator.newId();
        FiscalNumberResult fiscalNumber = assignFiscalNumberUseCase.assign(new AssignFiscalNumberCommand(
                sale.companyId(), documentType, LocalDate.ofInstant(now, ZoneOffset.UTC), FiscalEnvironment.TEST));
        ProviderSubmissionResult provider = providerPort.submit(sale, documentId, documentType, idempotencyKey);
        ElectronicDocument document = documentFromProvider(documentId, sale, documentType, fiscalNumber, provider,
                idempotencyKey, now);
        Sale confirmed = saleRepository.save(sale.confirm(document, now));
        Sale completed = applyPostValidationEffects(confirmed);
        publishConfirmedSaleEvents(completed);
        auditEventPort.register(toAuditEvent(completed));
        return BillingResultMapper.toSaleResult(completed);
    }

    @Override
    public SaleResult overrideDocumentType(SaleDocumentTypeOverrideCommand command) {
        Objects.requireNonNull(command, "command is required");
        Sale sale = saleRepository.findByCompanyIdAndId(command.companyId(), command.saleId())
                .orElseThrow(() -> new SaleNotFoundException(command.saleId()));
        if (sale.status() != SaleStatus.DRAFT) {
            throw new IllegalStateException("Solo puedes cambiar el tipo de documento fiscal antes de emitir la venta.");
        }
        if (command.documentType() == null || !command.documentType().isSaleDocument()) {
            throw new IllegalArgumentException("El tipo de documento fiscal no es valido para ventas.");
        }
        CompanyFiscalPolicy policy = companyFiscalPolicyRepository.findByCompanyId(command.companyId())
                .orElseGet(() -> CompanyFiscalPolicy.defaults(command.companyId()));
        if (!policy.allowDocumentTypeOverride()) {
            throw new IllegalStateException("La politica fiscal de la empresa no permite cambios excepcionales.");
        }
        if (policy.requirePinForOverride()) {
            OperationalPinValidationPort.OperationalPinValidationResult pinResult = operationalPinValidationPort.verify(
                    command.companyId(), command.pin(), command.authorizationHeader());
            if (!pinResult.valid()) {
                throw new IllegalStateException(pinResult.locked() || pinResult.mustChange()
                        ? "El PIN operacional esta bloqueado o requiere cambio."
                        : "El PIN operacional no es valido. Intentos restantes: " + pinResult.remainingAttempts() + ".");
            }
        }
        saleDocumentTypeOverrideRepository.save(SaleDocumentTypeOverride.create(idGenerator.newId(),
                command.companyId(), command.saleId(), command.documentType(), command.authorizedBy(),
                command.reason(), clock.now()));
        return BillingResultMapper.toSaleResult(sale);
    }

    @Override
    public List<SaleResult> find(SaleQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        return saleRepository.find(query).stream().map(BillingResultMapper::toSaleResult).toList();
    }

    @Override
    public List<ElectronicDocumentResult> findElectronicDocuments(ElectronicDocumentQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        return saleRepository.findByElectronicDocument(query).stream()
                .map(Sale::electronicDocument)
                .filter(Objects::nonNull)
                .map(BillingResultMapper::toDocumentResult)
                .toList();
    }

    @Override
    public ElectronicDocumentResult findElectronicDocument(UUID companyId, UUID documentId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        Sale sale = saleRepository.findByCompanyIdAndElectronicDocumentId(companyId, documentId)
                .orElseThrow(() -> new SaleNotFoundException(documentId));
        return BillingResultMapper.toDocumentResult(sale.electronicDocument());
    }

    @Override
    public List<FiscalArtifactResult> findArtifacts(UUID companyId, UUID documentId) {
        ElectronicDocumentResult document = findElectronicDocument(companyId, documentId);
        return List.of(
                new FiscalArtifactResult("XML", "mock://billing/" + document.id() + ".xml",
                        "mock-hash-xml-" + document.cufeCude(),
                        "<Document id=\"" + document.id() + "\" type=\"" + document.documentType() + "\" />"),
                new FiscalArtifactResult("PDF", "mock://billing/" + document.id() + ".pdf",
                        "mock-hash-pdf-" + document.cufeCude(),
                        "mock-graphic-representation:" + document.cufeCude()),
                new FiscalArtifactResult("QR", "mock://billing/" + document.id() + ".qr",
                        "mock-hash-qr-" + document.cufeCude(), document.qrContent()));
    }

    @Override
    public List<FiscalEventResult> findFiscalEvents(UUID companyId, UUID documentId) {
        ElectronicDocumentResult document = findElectronicDocument(companyId, documentId);
        return List.of(new FiscalEventResult(document.id(), "PROVIDER_SUBMISSION", document.status().name(),
                "providerStatus=" + document.providerStatus() + ";trackingId=" + valueOrEmpty(document.providerTrackingId()),
                document.issuedAt()));
    }
    @Override
    public SaleResult findById(UUID companyId, UUID saleId) {
        return BillingResultMapper.toSaleResult(saleRepository.findByCompanyIdAndId(companyId, saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId)));
    }

    @Override
    public PosReceiptResult printableReceipt(UUID companyId, UUID saleId, int widthMm) {
        SaleResult sale = findById(companyId, saleId);
        if (sale.electronicDocument() == null) {
            throw new IllegalStateException("sale has no electronic document");
        }
        return PosReceiptRenderer.render(sale, widthMm);
    }

    private SaleResult createNew(CreateSaleCommand command) {
        licenseValidationPort.ensureAllowed(command.companyId(), LicenseAction.CREATE_TRANSACTION);
        BuyerIdentificationMode buyerMode = resolveBuyerMode(command);
        var lines = command.lines().stream().map(line -> toLine(command.companyId(), line)).toList();
        lines.forEach(line -> ensureAvailable(command.companyId(), line));
        Sale sale = Sale.draft(idGenerator.newId(), command.companyId(), buyerMode, command.customerId(),
                command.paymentMethodCode() == null ? PaymentMethodCode.CASH : command.paymentMethodCode(),
                command.virtualWalletCode(),
                command.saleChannel() == null ? SaleChannel.POS : command.saleChannel(), command.idempotencyKey(),
                command.createdBy(), clock.now(), lines);
        return BillingResultMapper.toSaleResult(saleRepository.save(sale));
    }

    private SaleLine toLine(UUID companyId, SaleLineCommand command) {
        InventoryProductSnapshot product = inventoryAvailability.findProduct(companyId, command.productId());
        if (!product.saleEnabled()) {
            throw new IllegalStateException("product is not enabled for sale");
        }
        BigDecimal unitPrice = product.salePrice() == null || product.salePrice().signum() == 0
                ? command.unitPrice()
                : product.salePrice();
        return SaleLine.calculate(idGenerator.newId(), command.productId(), product.sku(), product.name(),
                product.itemType(), product.stockTracked(), command.quantity(), unitPrice, product.cost(),
                command.discountAmount(), product.taxCode(), product.taxRate());
    }

    private BuyerIdentificationMode resolveBuyerMode(CreateSaleCommand command) {
        BuyerIdentificationMode buyerMode = command.buyerIdentificationMode() == null
                ? (command.customerId() == null ? BuyerIdentificationMode.FINAL_CONSUMER : BuyerIdentificationMode.IDENTIFIED_CUSTOMER)
                : command.buyerIdentificationMode();
        if (buyerMode == BuyerIdentificationMode.IDENTIFIED_CUSTOMER && command.customerId() == null) {
            throw new IllegalArgumentException("customerId is required for identified customer sales");
        }
        if (buyerMode == BuyerIdentificationMode.FINAL_CONSUMER) {
            if (command.customerId() != null) {
                throw new IllegalArgumentException("customerId must be empty for final consumer sales");
            }
            finalConsumerProfileRepository.findActiveForCompanyOrGlobal(command.companyId())
                    .orElseThrow(() -> new IllegalStateException("active final consumer profile is required"));
        }
        return buyerMode;
    }

    private void ensureAvailable(UUID companyId, SaleLine line) {
        if (!line.affectsInventory()) {
            return;
        }
        if (!inventoryAvailability.isAvailable(companyId, line.productId(), line.quantity())) {
            throw new InsufficientStockException(line.productId());
        }
    }

    private void ensureMonthlyDocumentQuota(UUID companyId, LicensePolicy policy, Instant issuedAt) {
        Integer maxMonthlyDocuments = policy.maxMonthlyDocuments();
        if (maxMonthlyDocuments == null) {
            return;
        }
        LocalDate documentDate = LocalDate.ofInstant(issuedAt, ZoneOffset.UTC);
        Instant from = documentDate.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = documentDate.withDayOfMonth(1).plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long issuedDocuments = fiscalDocumentUsagePort.countIssuedDocuments(companyId, from, to);
        if (issuedDocuments >= maxMonthlyDocuments.longValue()) {
            throw new LicenseBlockedException("La licencia permite maximo " + maxMonthlyDocuments
                    + " documentos fiscales mensuales.");
        }
    }

    private ElectronicDocumentType resolveSaleDocumentType(UUID companyId) {
        return companyFiscalPolicyRepository.findByCompanyId(companyId)
                .orElseGet(() -> CompanyFiscalPolicy.defaults(companyId))
                .resolveSaleDocumentType();
    }

    private ElectronicDocumentType resolveSaleDocumentType(Sale sale) {
        return saleDocumentTypeOverrideRepository.findActiveByCompanyIdAndSaleId(sale.companyId(), sale.id())
                .map(SaleDocumentTypeOverride::documentType)
                .orElseGet(() -> resolveSaleDocumentType(sale.companyId()));
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

    private void publishConfirmedSaleEvents(Sale sale) {
        ElectronicDocument document = sale.electronicDocument();
        if (document == null) {
            return;
        }
        if (document.status() == ElectronicDocumentStatus.VALIDATED) {
            eventPublisher.publish(event(EventTypes.SALE_CONFIRMED, sale, document, salePayload(sale, document),
                    document.idempotencyKey() + ":sale-confirmed"));
            eventPublisher.publish(event(EventTypes.ELECTRONIC_DOCUMENT_VALIDATED, sale, document,
                    electronicDocumentPayload(sale, document), document.idempotencyKey() + ":document-validated"));
        }
        if (document.status() == ElectronicDocumentStatus.FAILED) {
            eventPublisher.publish(event(EventTypes.PROVIDER_SUBMISSION_FAILED, sale, document,
                    providerRetryPayload(sale, document), document.idempotencyKey() + ":provider-submission-failed"));
        }
        eventPublisher.publish(event(EventTypes.AUDIT_EVENT_REQUESTED, sale, document, auditPayload(sale),
                document.idempotencyKey() + ":audit-requested"));
    }

    private DomainEventEnvelope event(String eventType, Sale sale, ElectronicDocument document,
            Map<String, Object> payload, String idempotencyKey) {
        return new DomainEventEnvelope(idGenerator.newId(), eventType, 1, clock.now(), sale.companyId(), "Sale",
                sale.id(), "billing-service", null, idempotencyKey, payload);
    }

    private static Map<String, Object> salePayload(Sale sale, ElectronicDocument document) {
        Map<String, Object> payload = electronicDocumentPayload(sale, document);
        payload.put("saleChannel", sale.saleChannel().name());
        payload.put("saleStatus", sale.status().name());
        payload.put("customerId", sale.customerId() == null ? null : sale.customerId().toString());
        payload.put("subtotal", sale.subtotal());
        payload.put("taxTotal", sale.taxTotal());
        payload.put("lines", sale.lines().stream().map(SaleManagementService::saleLinePayload).toList());
        payload.put("inventoryApplied", document.inventoryApplied());
        payload.put("accountingApplied", document.accountingApplied());
        return payload;
    }


    private static Map<String, Object> saleLinePayload(SaleLine line) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("lineId", line.id().toString());
        payload.put("productId", line.productId().toString());
        payload.put("productSku", line.productSku());
        payload.put("productName", line.productName());
        payload.put("itemType", line.itemType().name());
        payload.put("stockTracked", line.stockTracked());
        payload.put("quantity", line.quantity());
        payload.put("unitCost", line.unitCost());
        payload.put("unitPrice", line.unitPrice());
        payload.put("subtotal", line.subtotal());
        payload.put("taxAmount", line.taxAmount());
        payload.put("total", line.total());
        return payload;
    }
    private static Map<String, Object> electronicDocumentPayload(Sale sale, ElectronicDocument document) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("saleId", sale.id().toString());
        payload.put("documentId", document.id().toString());
        payload.put("documentType", document.documentType().name());
        payload.put("documentStatus", document.status().name());
        payload.put("providerStatus", document.providerStatus().name());
        payload.put("prefix", document.prefix());
        payload.put("documentNumber", document.documentNumber());
        payload.put("documentIdempotencyKey", document.idempotencyKey());
        payload.put("cufeCude", document.cufeCude());
        payload.put("total", document.total());
        payload.put("issuedAt", document.issuedAt().toString());
        return payload;
    }

    private static Map<String, Object> providerRetryPayload(Sale sale, ElectronicDocument document) {
        Map<String, Object> payload = salePayload(sale, document);
        payload.put("providerTrackingId", document.providerTrackingId());
        payload.put("providerErrorCode", document.providerErrorCode());
        payload.put("providerErrorMessage", document.providerErrorMessage());
        return payload;
    }
    private static Map<String, Object> auditPayload(Sale sale) {
        AuditEventCommand audit = toAuditEvent(sale);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("companyId", audit.companyId().toString());
        payload.put("userId", audit.userId() == null ? null : audit.userId().toString());
        payload.put("eventType", audit.eventType());
        payload.put("resourceType", audit.resourceType());
        payload.put("resourceId", audit.resourceId());
        payload.put("action", audit.action());
        payload.put("result", audit.result().name());
        payload.put("detail", audit.detail());
        return payload;
    }
    private static AuditEventCommand toAuditEvent(Sale sale) {
        ElectronicDocument document = sale.electronicDocument();
        AuditResult result = document.status() == ElectronicDocumentStatus.VALIDATED
                ? AuditResult.SUCCESS
                : AuditResult.FAILURE;
        String detail = """
                {"saleId":"%s","documentId":"%s","documentType":"%s","documentStatus":"%s","providerStatus":"%s","providerTrackingId":"%s","inventoryApplied":%s,"accountingApplied":%s}
                """.formatted(sale.id(), document.id(), document.documentType(), document.status(),
                document.providerStatus(), valueOrEmpty(document.providerTrackingId()), document.inventoryApplied(),
                document.accountingApplied()).trim();
        return new AuditEventCommand(sale.companyId(), sale.createdBy(), "ELECTRONIC_DOCUMENT", "SALE",
                sale.id().toString(), "CONFIRM_SALE", result, detail);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
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
