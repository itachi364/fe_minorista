package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAdjustmentNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateFiscalAdjustmentNoteUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAdjustmentNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAdjustmentNote;

public class CreateFiscalAdjustmentNoteService implements CreateFiscalAdjustmentNoteUseCase {

    private final ElectronicDocumentLifecycleRepositoryPort documentRepository;
    private final FiscalAdjustmentNoteRepositoryPort noteRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CreateFiscalAdjustmentNoteService(
            ElectronicDocumentLifecycleRepositoryPort documentRepository,
            FiscalAdjustmentNoteRepositoryPort noteRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.documentRepository = Objects.requireNonNull(documentRepository);
        this.noteRepository = Objects.requireNonNull(noteRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public FiscalAdjustmentNoteResult create(CreateFiscalAdjustmentNoteCommand command) {
        validate(command);

        ElectronicDocumentLifecycle referencedDocument = documentRepository.findByCompanyIdAndDocumentId(
                command.companyId(),
                command.referencedDocumentId())
                .orElseThrow(() -> new IllegalStateException("referenced document was not found"));

        FiscalAdjustmentNote note = FiscalAdjustmentNote.create(
                idGenerator.newId(),
                referencedDocument,
                command.documentType(),
                command.reason(),
                command.subtotal(),
                command.taxTotal(),
                command.total(),
                command.createdBy(),
                clock.now());

        FiscalAdjustmentNote savedNote = noteRepository.save(note);

        return new FiscalAdjustmentNoteResult(
                savedNote.id(),
                savedNote.companyId(),
                savedNote.referencedDocumentId(),
                savedNote.documentType(),
                savedNote.reason(),
                savedNote.subtotal(),
                savedNote.taxTotal(),
                savedNote.total(),
                savedNote.status(),
                savedNote.createdBy(),
                savedNote.createdAt());
    }

    private static void validate(CreateFiscalAdjustmentNoteCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.referencedDocumentId(), "referencedDocumentId is required");
        Objects.requireNonNull(command.documentType(), "documentType is required");
    }
}
