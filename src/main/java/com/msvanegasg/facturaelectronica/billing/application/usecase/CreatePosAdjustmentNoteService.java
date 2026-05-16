package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreatePosAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.PosAdjustmentNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreatePosAdjustmentNoteUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.PosAdjustmentNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNumberAssignment;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentNote;

public class CreatePosAdjustmentNoteService implements CreatePosAdjustmentNoteUseCase {

    private final ElectronicPosDocumentRepositoryPort posDocumentRepository;
    private final PosAdjustmentNoteRepositoryPort noteRepository;
    private final AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CreatePosAdjustmentNoteService(
            ElectronicPosDocumentRepositoryPort posDocumentRepository,
            PosAdjustmentNoteRepositoryPort noteRepository,
            AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.posDocumentRepository = Objects.requireNonNull(posDocumentRepository);
        this.noteRepository = Objects.requireNonNull(noteRepository);
        this.assignFiscalNumberUseCase = Objects.requireNonNull(assignFiscalNumberUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PosAdjustmentNoteResult create(CreatePosAdjustmentNoteCommand command) {
        validate(command);

        ElectronicPosDocument referencedDocument = posDocumentRepository.findByCompanyIdAndDocumentId(
                command.companyId(),
                command.referencedDocumentId())
                .orElseThrow(() -> new IllegalStateException("referenced POS document was not found"));

        FiscalNumberResult fiscalNumber = assignFiscalNumberUseCase.assign(new AssignFiscalNumberCommand(
                command.companyId(),
                ElectronicDocumentType.POS_ADJUSTMENT_NOTE,
                command.documentDate(),
                command.environment()));

        PosAdjustmentNote note = PosAdjustmentNote.create(
                idGenerator.newId(),
                referencedDocument,
                command.adjustmentType(),
                command.reason(),
                new FiscalNumberAssignment(
                        fiscalNumber.resolutionId(),
                        fiscalNumber.resolutionNumber(),
                        fiscalNumber.prefix(),
                        fiscalNumber.number()),
                command.subtotal(),
                command.taxTotal(),
                command.total(),
                command.createdBy(),
                clock.now());

        PosAdjustmentNote savedNote = noteRepository.save(note);

        return new PosAdjustmentNoteResult(
                savedNote.id(),
                savedNote.companyId(),
                savedNote.referencedDocumentId(),
                savedNote.adjustmentType(),
                savedNote.reason(),
                savedNote.prefix(),
                savedNote.number(),
                savedNote.subtotal(),
                savedNote.taxTotal(),
                savedNote.total(),
                savedNote.status(),
                savedNote.createdBy(),
                savedNote.createdAt());
    }

    private static void validate(CreatePosAdjustmentNoteCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.referencedDocumentId(), "referencedDocumentId is required");
        Objects.requireNonNull(command.adjustmentType(), "adjustmentType is required");
        Objects.requireNonNull(command.documentDate(), "documentDate is required");
        Objects.requireNonNull(command.environment(), "environment is required");
    }
}
