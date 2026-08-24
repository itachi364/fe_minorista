package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

class MockProviderSubmissionServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUBMISSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Test
    void submitsAcceptedMockDocument() {
        InMemoryRepository repository = new InMemoryRepository();
        MockProviderSubmissionService service = service(repository, properties("mock", ProviderSubmissionStatus.ACCEPTED,
                null, null));

        var result = service.submit(command("confirm-1"));

        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.ACCEPTED);
        assertThat(result.trackingId()).isEqualTo("mock-electronic_pos-" + DOCUMENT_ID);
        assertThat(result.cufeCude()).isNotBlank();
        assertThat(result.qrContent()).startsWith("mock-qr:");
        assertThat(repository.saved).isEqualTo(1);
    }

    @Test
    void submissionIsIdempotent() {
        InMemoryRepository repository = new InMemoryRepository();
        MockProviderSubmissionService service = service(repository, properties("mock", ProviderSubmissionStatus.ACCEPTED,
                null, null));

        var first = service.submit(command("confirm-1"));
        var second = service.submit(command("confirm-1"));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(repository.saved).isEqualTo(1);
    }

    @Test
    void returnsConfiguredRejectedResponse() {
        MockProviderSubmissionService service = service(new InMemoryRepository(),
                properties("mock", ProviderSubmissionStatus.REJECTED, "CUSTOM_REJECTED", "Rechazo simulado."));

        var result = service.submit(command("confirm-2"));

        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.REJECTED);
        assertThat(result.errorCode()).isEqualTo("CUSTOM_REJECTED");
        assertThat(result.errorMessage()).isEqualTo("Rechazo simulado.");
    }

    @Test
    void rejectsNonMockMode() {
        MockProviderSubmissionService service = service(new InMemoryRepository(), properties("real",
                ProviderSubmissionStatus.ACCEPTED, null, null));

        assertThatThrownBy(() -> service.submit(command("confirm-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DIAN_PROVIDER_MODE=mock");
    }

    private static MockProviderSubmissionService service(InMemoryRepository repository,
            DianProviderProperties properties) {
        IdGeneratorPort idGenerator = () -> SUBMISSION_ID;
        ClockPort clock = () -> NOW;
        return new MockProviderSubmissionService(repository, idGenerator, clock, properties);
    }

    private static SubmitProviderDocumentCommand command(String idempotencyKey) {
        return new SubmitProviderDocumentCommand(COMPANY_ID, DOCUMENT_ID, ProviderDocumentType.ELECTRONIC_POS,
                idempotencyKey, "{}");
    }

    private static DianProviderProperties properties(String mode, ProviderSubmissionStatus status, String errorCode,
            String errorMessage) {
        return new DianProviderProperties(mode, status, errorCode, errorMessage, null, null, null, null, null, null,
                null);
    }

    private static final class InMemoryRepository implements ProviderSubmissionRepositoryPort {

        private final Map<String, ProviderSubmission> submissions = new HashMap<>();
        private int saved;

        @Override
        public ProviderSubmission save(ProviderSubmission submission) {
            saved++;
            submissions.put(key(submission.companyId(), submission.documentId(), submission.documentType(),
                    submission.idempotencyKey()), submission);
            submissions.put(submission.trackingId(), submission);
            return submission;
        }

        @Override
        public Optional<ProviderSubmission> findByIdempotencyKey(UUID companyId, UUID documentId,
                ProviderDocumentType documentType, String idempotencyKey) {
            return Optional.ofNullable(submissions.get(key(companyId, documentId, documentType, idempotencyKey)));
        }

        @Override
        public Optional<ProviderSubmission> findByTrackingId(String trackingId) {
            return Optional.ofNullable(submissions.get(trackingId));
        }

        @Override
        public Optional<ProviderSubmission> findByCompanyIdAndTrackingId(UUID companyId, String trackingId) {
            return findByTrackingId(trackingId).filter(submission -> submission.companyId().equals(companyId));
        }

        private static String key(UUID companyId, UUID documentId, ProviderDocumentType documentType,
                String idempotencyKey) {
            return companyId + "|" + documentId + "|" + documentType + "|" + idempotencyKey;
        }
    }
}
