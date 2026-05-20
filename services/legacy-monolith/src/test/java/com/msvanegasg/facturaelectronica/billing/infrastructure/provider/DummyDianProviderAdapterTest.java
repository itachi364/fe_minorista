package com.msvanegasg.facturaelectronica.billing.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderRequest;
import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderResponse;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

class DummyDianProviderAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void defaultMockAcceptsDocumentWithDeterministicArtifacts() {
        DummyDianProviderAdapter adapter = new DummyDianProviderAdapter();

        DianProviderResponse response = adapter.submit(validRequest(ElectronicDocumentType.ELECTRONIC_POS));

        assertThat(response.status()).isEqualTo(ProviderSubmissionStatus.ACCEPTED);
        assertThat(response.providerSubmissionId()).isEqualTo("DUMMY-SUBMISSION-BBBBBBBB-BBB");
        assertThat(response.cufeCude()).isEqualTo("DUMMY-CUDE-BBBBBBBB-BBB");
        assertThat(response.qrContent()).contains(DOCUMENT_ID.toString());
        assertThat(response.xmlContent()).isEqualTo("<xml/>");
        assertThat(response.errorCode()).isNull();
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    void rejectedMockReturnsSafeConfigurableError() {
        DummyDianProviderAdapter adapter = new DummyDianProviderAdapter(
                ProviderSubmissionStatus.REJECTED,
                "MOCK-REJECTED",
                "mock rejection for local test");

        DianProviderResponse response = adapter.submit(validRequest(ElectronicDocumentType.ELECTRONIC_INVOICE));

        assertThat(response.status()).isEqualTo(ProviderSubmissionStatus.REJECTED);
        assertThat(response.providerSubmissionId()).isEqualTo("DUMMY-REJECTION-BBBBBBBB-BBB");
        assertThat(response.cufeCude()).isNull();
        assertThat(response.qrContent()).isNull();
        assertThat(response.errorCode()).isEqualTo("MOCK-REJECTED");
        assertThat(response.errorMessage()).isEqualTo("mock rejection for local test");
    }

    @Test
    void failedMockReturnsDefaultSafeFailureWhenErrorFieldsAreBlank() {
        DummyDianProviderAdapter adapter = new DummyDianProviderAdapter(
                ProviderSubmissionStatus.FAILED,
                " ",
                null);

        DianProviderResponse response = adapter.submit(validRequest(ElectronicDocumentType.ELECTRONIC_INVOICE));

        assertThat(response.status()).isEqualTo(ProviderSubmissionStatus.FAILED);
        assertThat(response.providerSubmissionId()).isEqualTo("DUMMY-FAILURE-BBBBBBBB-BBB");
        assertThat(response.errorCode()).isEqualTo("DUMMY_FAILED");
        assertThat(response.errorMessage()).isEqualTo("document submission failed in DIAN mock");
    }

    private static DianProviderRequest validRequest(ElectronicDocumentType documentType) {
        return new DianProviderRequest(
                COMPANY_ID,
                DOCUMENT_ID,
                documentType,
                "POS",
                1,
                new BigDecimal("30000.00"),
                new BigDecimal("5700.00"),
                new BigDecimal("35700.00"),
                "<xml/>",
                "idem-001");
    }
}
