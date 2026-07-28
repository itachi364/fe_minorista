package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageFiscalNoteUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CudeGenerator;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public class FiscalNoteManagementService implements ManageFiscalNoteUseCase {

    private final FiscalNoteRepositoryPort noteRepository;
    private final SaleRepositoryPort saleRepository;
    private final FiscalNoteProviderPort providerPort;
    private final AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public FiscalNoteManagementService(FiscalNoteRepositoryPort noteRepository, SaleRepositoryPort saleRepository,
            FiscalNoteProviderPort providerPort, AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.noteRepository = Objects.requireNonNull(noteRepository);
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.providerPort = Objects.requireNonNull(providerPort);
        this.assignFiscalNumberUseCase = Objects.requireNonNull(assignFiscalNumberUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public FiscalNoteResult create(CreateFiscalNoteCommand command) {
        validate(command);
        return noteRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey())
                .map(BillingResultMapper::toFiscalNoteResult)
                .orElseGet(() -> BillingResultMapper.toFiscalNoteResult(createNew(command)));
    }

    @Override
    public FiscalNoteResult findById(UUID companyId, UUID noteId) {
        return BillingResultMapper.toFiscalNoteResult(noteRepository.findByCompanyIdAndId(companyId, noteId)
                .orElseThrow(() -> new SaleNotFoundException(noteId)));
    }

    private FiscalNote createNew(CreateFiscalNoteCommand command) {
        Sale sale = saleRepository.findByCompanyIdAndElectronicDocumentId(command.companyId(), command.originalDocumentId())
                .orElseThrow(() -> new SaleNotFoundException(command.originalDocumentId()));
        ElectronicDocument original = sale.electronicDocument();
        validateOriginal(command, original);
        UUID noteId = idGenerator.newId();
        var now = clock.now();
        ElectronicDocumentType documentType = documentType(command.noteType());
        FiscalNumberResult number = assignFiscalNumberUseCase.assign(new AssignFiscalNumberCommand(command.companyId(),
                documentType, LocalDate.ofInstant(now, ZoneOffset.UTC), FiscalEnvironment.TEST));
        ProviderSubmissionResult provider = providerPort.submit(command.companyId(), noteId, documentType,
                payload(command, original), command.idempotencyKey());
        ProviderStatus providerStatus = provider.status();
        ElectronicDocumentStatus status = switch (providerStatus) {
            case ACCEPTED -> ElectronicDocumentStatus.VALIDATED;
            case REJECTED -> ElectronicDocumentStatus.REJECTED;
            case FAILED -> ElectronicDocumentStatus.FAILED;
        };
        String cude = provider.cufeCude() == null || provider.cufeCude().isBlank()
                ? CudeGenerator.generate(command.companyId() + "|" + noteId + "|" + documentType + "|" + command.total())
                : provider.cufeCude();
        String qr = provider.qrContent() == null || provider.qrContent().isBlank() ? "mock-qr:" + cude
                : provider.qrContent();
        FiscalNote note = new FiscalNote(noteId, command.companyId(), command.originalDocumentId(), command.noteType(),
                command.adjustmentKind(), status, providerStatus, command.reason(), number.prefix(), number.number(),
                cude, qr, command.subtotal(), command.taxTotal(), command.total(), provider.trackingId(),
                provider.errorCode(), provider.errorMessage(), command.idempotencyKey(), now);
        return noteRepository.save(note);
    }

    private static Map<String, Object> payload(CreateFiscalNoteCommand command, ElectronicDocument original) {
        return Map.of(
                "originalDocumentId", original.id(),
                "originalDocumentType", original.documentType().name(),
                "originalPrefix", original.prefix(),
                "originalNumber", original.documentNumber(),
                "originalCufeCude", original.cufeCude(),
                "noteType", command.noteType().name(),
                "reason", command.reason(),
                "total", command.total());
    }

    private static void validateOriginal(CreateFiscalNoteCommand command, ElectronicDocument original) {
        if (original == null || original.status() != ElectronicDocumentStatus.VALIDATED) {
            throw new IllegalStateException("original document must be validated");
        }
        if ((command.noteType() == FiscalNoteType.CREDIT_NOTE || command.noteType() == FiscalNoteType.DEBIT_NOTE)
                && original.documentType() != ElectronicDocumentType.ELECTRONIC_INVOICE) {
            throw new IllegalStateException("credit and debit notes require an electronic invoice");
        }
        if (command.noteType() == FiscalNoteType.POS_ADJUSTMENT_NOTE
                && original.documentType() != ElectronicDocumentType.ELECTRONIC_POS) {
            throw new IllegalStateException("POS adjustment notes require an electronic POS document");
        }
    }

    private static ElectronicDocumentType documentType(FiscalNoteType noteType) {
        return switch (noteType) {
            case CREDIT_NOTE -> ElectronicDocumentType.CREDIT_NOTE;
            case DEBIT_NOTE -> ElectronicDocumentType.DEBIT_NOTE;
            case POS_ADJUSTMENT_NOTE -> ElectronicDocumentType.POS_ADJUSTMENT_NOTE;
        };
    }

    private static void validate(CreateFiscalNoteCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.originalDocumentId(), "originalDocumentId is required");
        Objects.requireNonNull(command.noteType(), "noteType is required");
        Objects.requireNonNull(command.reason(), "reason is required");
        Objects.requireNonNull(command.subtotal(), "subtotal is required");
        Objects.requireNonNull(command.taxTotal(), "taxTotal is required");
        Objects.requireNonNull(command.total(), "total is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");
        if (command.reason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
    }
}