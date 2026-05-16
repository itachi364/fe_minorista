package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderResponse;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicDocumentToProviderCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.DianProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ProviderSubmissionRecordRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionRecord;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.billing.infrastructure.provider.DummyDianProviderAdapter;

class SubmitElectronicDocumentToProviderServiceTest {

    private static final UUID SUBMISSION_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final Instant NOW = Instant.parse("2026-05-11T22:30:00Z");

    @Test
    void submitAcceptedDocumentStoresProviderArtifacts() {
        CapturingSubmissionRecordRepository repository = new CapturingSubmissionRecordRepository();
        SubmitElectronicDocumentToProviderService service = new SubmitElectronicDocumentToProviderService(
                request -> new DianProviderResponse(
                        ProviderSubmissionStatus.ACCEPTED,
                        "PT-001",
                        "CUFE-001",
                        "https://qr.example/001",
                        "<xml/>",
                        "PDF-CONTENT",
                        null,
                        null),
                repository,
                () -> SUBMISSION_ID,
                () -> NOW);

        ProviderSubmissionResult result = service.submit(validCommand());

        assertThat(result.submissionId()).isEqualTo(SUBMISSION_ID);
        assertThat(result.documentId()).isEqualTo(DOCUMENT_ID);
        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.ACCEPTED);
        assertThat(result.providerSubmissionId()).isEqualTo("PT-001");
        assertThat(result.cufeCude()).isEqualTo("CUFE-001");
        assertThat(result.qrContent()).isEqualTo("https://qr.example/001");
        assertThat(result.xmlContent()).isEqualTo("<xml/>");
        assertThat(result.graphicRepresentationContent()).isEqualTo("PDF-CONTENT");
        assertThat(repository.savedRecord().requestPayloadHash()).hasSize(64);
        assertThat(repository.savedRecord().submittedAt()).isEqualTo(NOW);
    }

    @Test
    void submitRejectedDocumentStoresProviderError() {
        CapturingSubmissionRecordRepository repository = new CapturingSubmissionRecordRepository();
        SubmitElectronicDocumentToProviderService service = new SubmitElectronicDocumentToProviderService(
                request -> new DianProviderResponse(
                        ProviderSubmissionStatus.REJECTED,
                        "PT-002",
                        null,
                        null,
                        null,
                        null,
                        "DIAN-001",
                        "document rejected"),
                repository,
                () -> SUBMISSION_ID,
                () -> NOW);

        ProviderSubmissionResult result = service.submit(validCommand());

        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.REJECTED);
        assertThat(result.providerSubmissionId()).isEqualTo("PT-002");
        assertThat(result.errorCode()).isEqualTo("DIAN-001");
        assertThat(result.errorMessage()).isEqualTo("document rejected");
        assertThat(repository.savedRecord().status()).isEqualTo(ProviderSubmissionStatus.REJECTED);
    }

    @Test
    void submitProviderFailureStoresSanitizedError() {
        CapturingSubmissionRecordRepository repository = new CapturingSubmissionRecordRepository();
        SubmitElectronicDocumentToProviderService service = new SubmitElectronicDocumentToProviderService(
                request -> {
                    throw new IllegalStateException("token secret leaked by provider");
                },
                repository,
                () -> SUBMISSION_ID,
                () -> NOW);

        ProviderSubmissionResult result = service.submit(validCommand());

        assertThat(result.status()).isEqualTo(ProviderSubmissionStatus.FAILED);
        assertThat(result.errorCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(result.errorMessage()).isEqualTo("provider submission failed");
        assertThat(repository.savedRecord().errorMessage()).doesNotContain("secret");
    }

    @Test
    void submitRejectsMissingPayloadXml() {
        SubmitElectronicDocumentToProviderService service = new SubmitElectronicDocumentToProviderService(
                request -> null,
                record -> record,
                () -> SUBMISSION_ID,
                () -> NOW);
        SubmitElectronicDocumentToProviderCommand command = new SubmitElectronicDocumentToProviderCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                "FE",
                1,
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1190.00"),
                " ",
                "idem-001");

        assertThatThrownBy(() -> service.submit(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("payloadXml is required");
    }

    @Test
    void dummyProviderReturnsDeterministicCudeForElectronicPos() {
        DummyDianProviderAdapter adapter = new DummyDianProviderAdapter();

        DianProviderResponse response = adapter.submit(new com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderRequest(
                COMPANY_ID,
                DOCUMENT_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                "POS",
                10,
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1190.00"),
                "<xml/>",
                "idem-001"));

        assertThat(response.status()).isEqualTo(ProviderSubmissionStatus.ACCEPTED);
        assertThat(response.providerSubmissionId()).isEqualTo("DUMMY-SUBMISSION-BBBBBBBB-BBB");
        assertThat(response.cufeCude()).isEqualTo("DUMMY-CUDE-BBBBBBBB-BBB");
        assertThat(response.qrContent()).contains(DOCUMENT_ID.toString());
    }

    private static SubmitElectronicDocumentToProviderCommand validCommand() {
        return new SubmitElectronicDocumentToProviderCommand(
                COMPANY_ID,
                DOCUMENT_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                "FE",
                1,
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1190.00"),
                "<xml/>",
                "idem-001");
    }

    private static final class CapturingSubmissionRecordRepository implements ProviderSubmissionRecordRepositoryPort {

        private ProviderSubmissionRecord savedRecord;

        @Override
        public ProviderSubmissionRecord save(ProviderSubmissionRecord submissionRecord) {
            savedRecord = submissionRecord;
            return submissionRecord;
        }

        ProviderSubmissionRecord savedRecord() {
            return savedRecord;
        }
    }
}
