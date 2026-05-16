package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentStatusResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterProviderSubmissionOutcomeCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentTraceEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceEvent;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

class RegisterProviderSubmissionOutcomeServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID DOCUMENT_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID USER_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID TRACE_ID = UUID.fromString("12121212-1212-1212-1212-121212121212");
    private static final UUID AUDIT_ID = UUID.fromString("34343434-3434-3434-3434-343434343434");
    private static final Instant PREVIOUS_TIME = Instant.parse("2026-05-11T20:00:00Z");
    private static final Instant NOW = Instant.parse("2026-05-11T23:00:00Z");

    @Test
    void registerAcceptedProviderOutcomeMovesDocumentToValidatedAndStoresArtifacts() {
        InMemoryDocumentRepository documentRepository = new InMemoryDocumentRepository(documentInStatus(
                ElectronicDocumentStatus.SENT_TO_PROVIDER));
        CapturingTraceRepository traceRepository = new CapturingTraceRepository();
        CapturingAuditRepository auditRepository = new CapturingAuditRepository();
        RegisterProviderSubmissionOutcomeService service = service(
                documentRepository,
                traceRepository,
                auditRepository);

        ElectronicDocumentStatusResult result = service.register(new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.ACCEPTED,
                "PT-001",
                "CUFE-001",
                "QR-001",
                "<xml/>",
                "PDF-CONTENT",
                null,
                null,
                USER_ID));

        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.VALIDATED);
        assertThat(result.providerSubmissionId()).isEqualTo("PT-001");
        assertThat(result.cufeCude()).isEqualTo("CUFE-001");
        assertThat(result.qrContent()).isEqualTo("QR-001");
        assertThat(result.xmlContent()).isEqualTo("<xml/>");
        assertThat(result.graphicRepresentationContent()).isEqualTo("PDF-CONTENT");
        assertThat(result.updatedAt()).isEqualTo(NOW);
        assertThat(traceRepository.savedEvent().previousStatus()).isEqualTo(ElectronicDocumentStatus.SENT_TO_PROVIDER);
        assertThat(traceRepository.savedEvent().newStatus()).isEqualTo(ElectronicDocumentStatus.VALIDATED);
        assertThat(traceRepository.savedEvent().action()).isEqualTo(ElectronicDocumentTraceAction.PROVIDER_ACCEPTED);
        assertThat(auditRepository.savedEvent().action()).isEqualTo("PROVIDER_ACCEPTED");
        assertThat(auditRepository.savedEvent().result()).isEqualTo("VALIDATED");
    }

    @Test
    void registerRejectedProviderOutcomeMovesDocumentToRejectedAndKeepsSafeError() {
        InMemoryDocumentRepository documentRepository = new InMemoryDocumentRepository(documentInStatus(
                ElectronicDocumentStatus.SENT_TO_PROVIDER));
        CapturingTraceRepository traceRepository = new CapturingTraceRepository();
        CapturingAuditRepository auditRepository = new CapturingAuditRepository();
        RegisterProviderSubmissionOutcomeService service = service(
                documentRepository,
                traceRepository,
                auditRepository);

        ElectronicDocumentStatusResult result = service.register(new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.REJECTED,
                "PT-002",
                null,
                null,
                null,
                null,
                "DIAN-001",
                "document rejected",
                USER_ID));

        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.REJECTED);
        assertThat(result.errorCode()).isEqualTo("DIAN-001");
        assertThat(result.errorMessage()).isEqualTo("document rejected");
        assertThat(traceRepository.savedEvent().action()).isEqualTo(ElectronicDocumentTraceAction.PROVIDER_REJECTED);
        assertThat(traceRepository.savedEvent().detail()).isEqualTo("DIAN-001: document rejected");
        assertThat(auditRepository.savedEvent().detail()).isEqualTo("DIAN-001: document rejected");
    }

    @Test
    void registerFailedProviderOutcomeMovesDocumentToFailed() {
        InMemoryDocumentRepository documentRepository = new InMemoryDocumentRepository(documentInStatus(
                ElectronicDocumentStatus.SENT_TO_PROVIDER));
        CapturingTraceRepository traceRepository = new CapturingTraceRepository();
        CapturingAuditRepository auditRepository = new CapturingAuditRepository();
        RegisterProviderSubmissionOutcomeService service = service(
                documentRepository,
                traceRepository,
                auditRepository);

        ElectronicDocumentStatusResult result = service.register(new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.FAILED,
                null,
                null,
                null,
                null,
                null,
                "PROVIDER_ERROR",
                "provider submission failed",
                USER_ID));

        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.FAILED);
        assertThat(traceRepository.savedEvent().action()).isEqualTo(ElectronicDocumentTraceAction.PROVIDER_FAILED);
        assertThat(auditRepository.savedEvent().result()).isEqualTo("FAILED");
    }

    @Test
    void registerAcceptedProviderOutcomeRejectsDocumentThatWasNotSent() {
        RegisterProviderSubmissionOutcomeService service = service(
                new InMemoryDocumentRepository(documentInStatus(ElectronicDocumentStatus.DRAFT)),
                new CapturingTraceRepository(),
                new CapturingAuditRepository());

        RegisterProviderSubmissionOutcomeCommand command = new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.ACCEPTED,
                "PT-001",
                "CUFE-001",
                "QR-001",
                "<xml/>",
                "PDF-CONTENT",
                null,
                null,
                USER_ID);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("document status must be SENT_TO_PROVIDER");
    }

    @Test
    void registerAcceptedProviderOutcomeRequiresCufeOrCude() {
        RegisterProviderSubmissionOutcomeService service = service(
                new InMemoryDocumentRepository(documentInStatus(ElectronicDocumentStatus.SENT_TO_PROVIDER)),
                new CapturingTraceRepository(),
                new CapturingAuditRepository());

        RegisterProviderSubmissionOutcomeCommand command = new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.ACCEPTED,
                "PT-001",
                " ",
                "QR-001",
                "<xml/>",
                "PDF-CONTENT",
                null,
                null,
                USER_ID);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cufeCude is required");
    }

    @Test
    void registerProviderOutcomeRejectsMissingDocument() {
        RegisterProviderSubmissionOutcomeService service = service(
                new InMemoryDocumentRepository(null),
                new CapturingTraceRepository(),
                new CapturingAuditRepository());

        RegisterProviderSubmissionOutcomeCommand command = new RegisterProviderSubmissionOutcomeCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ProviderSubmissionStatus.REJECTED,
                "PT-001",
                null,
                null,
                null,
                null,
                "DIAN-001",
                "document rejected",
                USER_ID);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("electronic document was not found");
    }

    private static RegisterProviderSubmissionOutcomeService service(
            ElectronicDocumentLifecycleRepositoryPort documentRepository,
            ElectronicDocumentTraceEventRepositoryPort traceRepository,
            FiscalAuditEventRepositoryPort auditRepository) {
        UUID[] ids = { TRACE_ID, AUDIT_ID };
        int[] index = { 0 };
        return new RegisterProviderSubmissionOutcomeService(
                documentRepository,
                traceRepository,
                auditRepository,
                () -> ids[index[0]++],
                () -> NOW);
    }

    private static ElectronicDocumentLifecycle documentInStatus(ElectronicDocumentStatus status) {
        return ElectronicDocumentLifecycle.restore(
                DOCUMENT_ID,
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                status,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PREVIOUS_TIME);
    }

    private static final class InMemoryDocumentRepository implements ElectronicDocumentLifecycleRepositoryPort {

        private ElectronicDocumentLifecycle document;

        private InMemoryDocumentRepository(ElectronicDocumentLifecycle document) {
            this.document = document;
        }

        @Override
        public Optional<ElectronicDocumentLifecycle> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
            return Optional.ofNullable(document)
                    .filter(current -> current.companyId().equals(companyId) && current.id().equals(documentId));
        }

        @Override
        public ElectronicDocumentLifecycle save(ElectronicDocumentLifecycle document) {
            this.document = document;
            return document;
        }
    }

    private static final class CapturingTraceRepository implements ElectronicDocumentTraceEventRepositoryPort {

        private ElectronicDocumentTraceEvent savedEvent;

        @Override
        public ElectronicDocumentTraceEvent save(ElectronicDocumentTraceEvent event) {
            savedEvent = event;
            return event;
        }

        ElectronicDocumentTraceEvent savedEvent() {
            return savedEvent;
        }
    }

    private static final class CapturingAuditRepository implements FiscalAuditEventRepositoryPort {

        private FiscalAuditEvent savedEvent;

        @Override
        public FiscalAuditEvent save(FiscalAuditEvent event) {
            savedEvent = event;
            return event;
        }

        FiscalAuditEvent savedEvent() {
            return savedEvent;
        }
    }
}
