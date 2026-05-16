package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateElectronicDocumentDraftCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentDraftResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateElectronicDocumentDraftUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentDraftRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentDraft;

public class CreateElectronicDocumentDraftService implements CreateElectronicDocumentDraftUseCase {

    private final ElectronicDocumentDraftRepositoryPort draftRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CreateElectronicDocumentDraftService(
            ElectronicDocumentDraftRepositoryPort draftRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.draftRepository = Objects.requireNonNull(draftRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ElectronicDocumentDraftResult createDraft(CreateElectronicDocumentDraftCommand command) {
        Objects.requireNonNull(command, "command is required");

        ElectronicDocumentDraft draft = ElectronicDocumentDraft.create(
                idGenerator.newId(),
                command.companyId(),
                command.documentType(),
                command.idempotencyKey(),
                clock.now());

        ElectronicDocumentDraft savedDraft = draftRepository.save(draft);

        return new ElectronicDocumentDraftResult(
                savedDraft.id(),
                savedDraft.companyId(),
                savedDraft.documentType(),
                savedDraft.status(),
                savedDraft.createdAt());
    }
}
