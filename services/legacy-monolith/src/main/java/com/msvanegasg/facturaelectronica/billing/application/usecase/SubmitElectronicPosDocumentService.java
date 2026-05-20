package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentStatusResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterProviderSubmissionOutcomeCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicDocumentToProviderCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.RegisterProviderSubmissionOutcomeUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicDocumentToProviderUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;

public class SubmitElectronicPosDocumentService implements SubmitElectronicPosDocumentUseCase {

    private final ElectronicPosDocumentRepositoryPort posDocumentRepository;
    private final ElectronicDocumentLifecycleRepositoryPort lifecycleRepository;
    private final SubmitElectronicDocumentToProviderUseCase providerSubmissionUseCase;
    private final RegisterProviderSubmissionOutcomeUseCase registerOutcomeUseCase;
    private final ClockPort clock;

    public SubmitElectronicPosDocumentService(
            ElectronicPosDocumentRepositoryPort posDocumentRepository,
            ElectronicDocumentLifecycleRepositoryPort lifecycleRepository,
            SubmitElectronicDocumentToProviderUseCase providerSubmissionUseCase,
            RegisterProviderSubmissionOutcomeUseCase registerOutcomeUseCase,
            ClockPort clock) {
        this.posDocumentRepository = Objects.requireNonNull(posDocumentRepository);
        this.lifecycleRepository = Objects.requireNonNull(lifecycleRepository);
        this.providerSubmissionUseCase = Objects.requireNonNull(providerSubmissionUseCase);
        this.registerOutcomeUseCase = Objects.requireNonNull(registerOutcomeUseCase);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public SubmitElectronicPosDocumentResult submit(UUID companyId, UUID documentId, String idempotencyKey) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }

        ElectronicPosDocument document = posDocumentRepository.findByCompanyIdAndDocumentId(companyId, documentId)
                .orElseThrow(() -> new IllegalStateException("electronic POS document was not found"));
        markAsSentToProvider(document);

        ProviderSubmissionResult providerResult = providerSubmissionUseCase.submit(
                new SubmitElectronicDocumentToProviderCommand(
                        companyId,
                        document.id(),
                        ElectronicDocumentType.ELECTRONIC_POS,
                        document.prefix(),
                        document.number(),
                        document.subtotal(),
                        document.taxTotal(),
                        document.total(),
                        payloadXml(document),
                        idempotencyKey.trim()));

        ElectronicDocumentStatusResult statusResult = registerOutcomeUseCase.register(
                new RegisterProviderSubmissionOutcomeCommand(
                        companyId,
                        document.id(),
                        providerResult.status(),
                        providerResult.providerSubmissionId(),
                        providerResult.cufeCude(),
                        providerResult.qrContent(),
                        providerResult.xmlContent(),
                        providerResult.graphicRepresentationContent(),
                        providerResult.errorCode(),
                        providerResult.errorMessage(),
                        null));

        return new SubmitElectronicPosDocumentResult(
                document.id(),
                providerResult.providerSubmissionId(),
                providerResult.status(),
                statusResult.status(),
                statusResult.cufeCude(),
                statusResult.qrContent(),
                statusResult.errorCode(),
                statusResult.errorMessage());
    }

    private void markAsSentToProvider(ElectronicPosDocument document) {
        lifecycleRepository.save(ElectronicDocumentLifecycle.restore(
                document.id(),
                document.companyId(),
                ElectronicDocumentType.ELECTRONIC_POS,
                ElectronicDocumentStatus.SENT_TO_PROVIDER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                clock.now()));
    }

    private static String payloadXml(ElectronicPosDocument document) {
        return "<mockElectronicPosDocument id=\"" + document.id() + "\" prefix=\""
                + document.prefix() + "\" number=\"" + document.number() + "\" total=\""
                + document.total() + "\"/>";
    }
}
