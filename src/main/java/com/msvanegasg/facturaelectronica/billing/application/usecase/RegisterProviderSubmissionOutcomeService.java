package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentStatusResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterProviderSubmissionOutcomeCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.RegisterProviderSubmissionOutcomeUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentTraceEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceEvent;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public class RegisterProviderSubmissionOutcomeService implements RegisterProviderSubmissionOutcomeUseCase {

    private static final String RESOURCE_TYPE = "ELECTRONIC_DOCUMENT";

    private final ElectronicDocumentLifecycleRepositoryPort documentRepository;
    private final ElectronicDocumentTraceEventRepositoryPort traceEventRepository;
    private final FiscalAuditEventRepositoryPort auditEventRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterProviderSubmissionOutcomeService(
            ElectronicDocumentLifecycleRepositoryPort documentRepository,
            ElectronicDocumentTraceEventRepositoryPort traceEventRepository,
            FiscalAuditEventRepositoryPort auditEventRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.documentRepository = Objects.requireNonNull(documentRepository);
        this.traceEventRepository = Objects.requireNonNull(traceEventRepository);
        this.auditEventRepository = Objects.requireNonNull(auditEventRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ElectronicDocumentStatusResult register(RegisterProviderSubmissionOutcomeCommand command) {
        validate(command);

        ElectronicDocumentLifecycle document = documentRepository.findByCompanyIdAndDocumentId(
                command.companyId(),
                command.documentId())
                .orElseThrow(() -> new IllegalStateException("electronic document was not found"));

        ElectronicDocumentStatus previousStatus = document.previousStatusFor(command.providerStatus());
        var occurredAt = clock.now();

        document.applyProviderOutcome(
                command.providerStatus(),
                command.providerSubmissionId(),
                command.cufeCude(),
                command.qrContent(),
                command.xmlContent(),
                command.graphicRepresentationContent(),
                command.errorCode(),
                command.errorMessage(),
                occurredAt);

        ElectronicDocumentLifecycle savedDocument = documentRepository.save(document);
        ElectronicDocumentTraceAction action = actionFor(command.providerStatus());
        String detail = safeDetail(command.providerStatus(), command.errorCode(), command.errorMessage());

        traceEventRepository.save(new ElectronicDocumentTraceEvent(
                idGenerator.newId(),
                command.companyId(),
                command.documentId(),
                previousStatus,
                savedDocument.status(),
                action,
                command.providerStatus(),
                detail,
                command.userId(),
                occurredAt));

        auditEventRepository.save(new FiscalAuditEvent(
                idGenerator.newId(),
                command.companyId(),
                command.documentId(),
                RESOURCE_TYPE,
                action.name(),
                savedDocument.status().name(),
                command.userId(),
                occurredAt,
                detail));

        return new ElectronicDocumentStatusResult(
                savedDocument.id(),
                savedDocument.status(),
                savedDocument.providerSubmissionId(),
                savedDocument.cufeCude(),
                savedDocument.qrContent(),
                savedDocument.xmlContent(),
                savedDocument.graphicRepresentationContent(),
                savedDocument.errorCode(),
                savedDocument.errorMessage(),
                savedDocument.updatedAt());
    }

    private static ElectronicDocumentTraceAction actionFor(ProviderSubmissionStatus providerStatus) {
        return switch (providerStatus) {
            case ACCEPTED -> ElectronicDocumentTraceAction.PROVIDER_ACCEPTED;
            case REJECTED -> ElectronicDocumentTraceAction.PROVIDER_REJECTED;
            case FAILED -> ElectronicDocumentTraceAction.PROVIDER_FAILED;
        };
    }

    private static String safeDetail(
            ProviderSubmissionStatus providerStatus,
            String errorCode,
            String errorMessage) {
        if (providerStatus == ProviderSubmissionStatus.ACCEPTED) {
            return "provider accepted document";
        }
        String code = errorCode == null || errorCode.isBlank() ? "UNKNOWN" : errorCode.trim();
        String message = errorMessage == null || errorMessage.isBlank()
                ? "provider did not provide a safe detail"
                : errorMessage.trim();
        return code + ": " + message;
    }

    private static void validate(RegisterProviderSubmissionOutcomeCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.documentId(), "documentId is required");
        Objects.requireNonNull(command.providerStatus(), "providerStatus is required");
    }
}
