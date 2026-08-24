package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSubmissionTraceRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalArtifactPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalArtifactStoragePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianArtifactType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConfigurationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionArtifact;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEvent;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTechnicalValidationResult;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTestStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian.BasicDianTechnicalValidationAdapter;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian.ConfigurableDianTransportAdapter;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian.DefaultFiscalDocumentXmlBuilderAdapter;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian.ReferenceDianSignatureAdapter;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian.Sha256DianIdentifierCalculationAdapter;

class DianProviderSubmissionServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-08-24T10:00:00Z");

    @Test
    void submitsRealDocumentThroughTechnicalPipeline() {
        InMemorySubmissionRepository submissions = new InMemorySubmissionRepository();
        InMemoryConfigurationRepository configurations = new InMemoryConfigurationRepository(activeRealConfiguration());
        InMemoryTraceRepository traces = new InMemoryTraceRepository();
        DianProviderSubmissionService service = service(submissions, configurations, traces,
                properties("real", ProviderSubmissionStatus.ACCEPTED));

        var result = service.submit(command("real-1"));

        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.ACCEPTED);
        assertThat(result.trackingId()).isEqualTo("real-electronic_pos-" + DOCUMENT_ID);
        assertThat(result.cufeCude()).isNotBlank();
        assertThat(result.qrContent()).contains("CUFE_CUDE=");
        assertThat(submissions.saved).isEqualTo(1);
        assertThat(traces.events).extracting(DianSubmissionEvent::eventType)
                .extracting(Enum::name)
                .contains("XML_BUILT", "IDENTIFIERS_CALCULATED", "SIGNED", "VALIDATED", "TRANSMITTED", "ACCEPTED");
        assertThat(traces.validationResults).hasSize(4);
        assertThat(traces.artifacts).extracting(DianSubmissionArtifact::artifactType)
                .contains(DianArtifactType.UNSIGNED_XML, DianArtifactType.SIGNED_XML, DianArtifactType.QR,
                        DianArtifactType.APPLICATION_RESPONSE);
    }

    @Test
    void realSubmissionIsIdempotent() {
        InMemorySubmissionRepository submissions = new InMemorySubmissionRepository();
        DianProviderSubmissionService service = service(submissions,
                new InMemoryConfigurationRepository(activeRealConfiguration()), new InMemoryTraceRepository(),
                properties("real", ProviderSubmissionStatus.ACCEPTED));

        var first = service.submit(command("same-key"));
        var second = service.submit(command("same-key"));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(submissions.saved).isEqualTo(1);
    }

    @Test
    void rejectsRealModeWithoutActiveConfiguration() {
        DianCompanyConfiguration inactive = new DianCompanyConfiguration(UUID.randomUUID(), COMPANY_ID,
                DianConnectionMode.REAL, DianEnvironment.TEST, "software-id", "pin-ref", "key-ref", "cert-ref",
                "cert", "fingerprint", NOW.plusSeconds(86_400), "https://dian.example.test", "test-set", true,
                DianConfigurationStatus.READY_FOR_TEST, DianTestStatus.SUCCESS, NOW, "ok", UUID.randomUUID(), NOW,
                NOW);
        DianProviderSubmissionService service = service(new InMemorySubmissionRepository(),
                new InMemoryConfigurationRepository(inactive), new InMemoryTraceRepository(),
                properties("real", ProviderSubmissionStatus.ACCEPTED));

        assertThatThrownBy(() -> service.submit(command("real-2")))
                .isInstanceOf(DianConfigurationIncompleteException.class)
                .hasMessageContaining("no esta activa");
    }

    private static DianProviderSubmissionService service(InMemorySubmissionRepository submissions,
            InMemoryConfigurationRepository configurations, InMemoryTraceRepository traces,
            DianProviderProperties properties) {
        SequenceIdGenerator idGenerator = new SequenceIdGenerator();
        ClockPort clock = () -> NOW;
        FiscalArtifactStoragePort artifactStorage = (companyId, submissionId, documentId, type, contentType, fileName,
                content, createdAt) -> new DianSubmissionArtifact(idGenerator.generate(), companyId, submissionId,
                        documentId, type, "test", "test://" + submissionId + "/" + fileName, contentType, fileName,
                        "sha256:test", content == null ? 0L : (long) content.length(), createdAt, null);
        DianTechnicalArtifactPort artifacts = () -> {
        };
        return new DianProviderSubmissionService(submissions, configurations, artifacts,
                new DefaultFiscalDocumentXmlBuilderAdapter(), new Sha256DianIdentifierCalculationAdapter(),
                new ReferenceDianSignatureAdapter(), new BasicDianTechnicalValidationAdapter(idGenerator, clock),
                new ConfigurableDianTransportAdapter(properties, org.springframework.web.client.RestClient.builder()),
                artifactStorage, traces, idGenerator, clock, properties);
    }

    private static SubmitProviderDocumentCommand command(String idempotencyKey) {
        return new SubmitProviderDocumentCommand(COMPANY_ID, DOCUMENT_ID, ProviderDocumentType.ELECTRONIC_POS,
                idempotencyKey, "{\"total\":15000}");
    }

    private static DianCompanyConfiguration activeRealConfiguration() {
        return new DianCompanyConfiguration(UUID.randomUUID(), COMPANY_ID, DianConnectionMode.REAL, DianEnvironment.TEST,
                "software-id", "pin-ref", "key-ref", "cert-ref", "cert", "fingerprint", NOW.plusSeconds(86_400),
                "https://dian.example.test", "test-set", true, DianConfigurationStatus.ACTIVE, DianTestStatus.SUCCESS,
                NOW, "ok", UUID.randomUUID(), NOW, NOW);
    }

    private static DianProviderProperties properties(String mode, ProviderSubmissionStatus realStatus) {
        return new DianProviderProperties(mode, ProviderSubmissionStatus.ACCEPTED, null, null, "stub", realStatus,
                "build/test-dian-artifacts", "artifacts", "invoice.xsd", "credit.xsd", "debit.xsd", "model.sch",
                "compiled.xsl", "codes.sch");
    }

    private static final class SequenceIdGenerator implements IdGeneratorPort {
        private long sequence = 1;

        @Override
        public UUID generate() {
            return new UUID(0L, sequence++);
        }
    }

    private static final class InMemoryConfigurationRepository implements DianConfigurationRepositoryPort {
        private final DianCompanyConfiguration configuration;

        private InMemoryConfigurationRepository(DianCompanyConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public DianCompanyConfiguration save(DianCompanyConfiguration configuration) {
            return configuration;
        }

        @Override
        public Optional<DianCompanyConfiguration> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(configuration).filter(value -> value.companyId().equals(companyId));
        }
    }

    private static final class InMemoryTraceRepository implements DianSubmissionTraceRepositoryPort {
        private final List<DianSubmissionEvent> events = new ArrayList<>();
        private final List<DianTechnicalValidationResult> validationResults = new ArrayList<>();
        private final List<DianSubmissionArtifact> artifacts = new ArrayList<>();

        @Override
        public void saveEvent(DianSubmissionEvent event) {
            events.add(event);
        }

        @Override
        public void saveValidationResults(java.util.Collection<DianTechnicalValidationResult> results) {
            validationResults.addAll(results);
        }

        @Override
        public void saveArtifact(DianSubmissionArtifact artifact) {
            artifacts.add(artifact);
        }
    }

    private static final class InMemorySubmissionRepository implements ProviderSubmissionRepositoryPort {
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
