package com.msvanegasg.facturaelectronica.providerretry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

class ProcessProviderSubmissionRetryServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SALE_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID DOCUMENT_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-07-21T10:00:00Z");

    @Test
    void acceptedRetryMarksDocumentAndPublishesEvents() {
        FakeRepository repository = new FakeRepository(snapshot(ElectronicDocumentStatus.FAILED, ProviderStatus.FAILED));
        ProcessProviderSubmissionRetryService service = service(repository,
                snapshot -> new ProviderSubmissionOutcome(ProviderStatus.ACCEPTED, "track-1", "CUFE-OK", "QR-OK", null, null));

        ProviderSubmissionRetryResult result = service.process(event(EventTypes.PROVIDER_SUBMISSION_FAILED));

        assertThat(result.processed()).isTrue();
        assertThat(result.accepted()).isTrue();
        assertThat(repository.accepted).isTrue();
        assertThat(repository.publishedEvents).extracting(DomainEventEnvelope::eventType)
                .containsExactly(EventTypes.SALE_CONFIRMED, EventTypes.ELECTRONIC_DOCUMENT_VALIDATED);
    }

    @Test
    void rejectedRetryMarksDocumentWithoutPublishingPostValidationEvents() {
        FakeRepository repository = new FakeRepository(snapshot(ElectronicDocumentStatus.FAILED, ProviderStatus.FAILED));
        ProcessProviderSubmissionRetryService service = service(repository,
                snapshot -> new ProviderSubmissionOutcome(ProviderStatus.REJECTED, "track-2", null, null,
                        "DIAN_REJECTED", "Rejected"));

        ProviderSubmissionRetryResult result = service.process(event(EventTypes.PROVIDER_SUBMISSION_FAILED));

        assertThat(result.processed()).isTrue();
        assertThat(result.rejected()).isTrue();
        assertThat(repository.rejected).isTrue();
        assertThat(repository.publishedEvents).isEmpty();
    }

    @Test
    void validatedDocumentIsDuplicateAndDoesNotCallProvider() {
        FakeRepository repository = new FakeRepository(snapshot(ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED));
        ProcessProviderSubmissionRetryService service = service(repository, snapshot -> {
            throw new AssertionError("provider should not be called");
        });

        ProviderSubmissionRetryResult result = service.process(event(EventTypes.PROVIDER_SUBMISSION_FAILED));

        assertThat(result.duplicate()).isTrue();
    }

    @Test
    void providerFailureIsMarkedAndRethrownForSqsRetry() {
        FakeRepository repository = new FakeRepository(snapshot(ElectronicDocumentStatus.FAILED, ProviderStatus.FAILED));
        ProcessProviderSubmissionRetryService service = service(repository,
                snapshot -> new ProviderSubmissionOutcome(ProviderStatus.FAILED, "track-3", null, null,
                        "MOCK_FAILED", "Still failed"));

        assertThatThrownBy(() -> service.process(event(EventTypes.PROVIDER_SUBMISSION_FAILED)))
                .isInstanceOf(ProviderRetryStillFailedException.class);
        assertThat(repository.failed).isTrue();
    }

    @Test
    void unsupportedEventIsIgnored() {
        FakeRepository repository = new FakeRepository(snapshot(ElectronicDocumentStatus.FAILED, ProviderStatus.FAILED));
        ProcessProviderSubmissionRetryService service = service(repository, snapshot -> {
            throw new AssertionError("provider should not be called");
        });

        ProviderSubmissionRetryResult result = service.process(event(EventTypes.SALE_CONFIRMED));

        assertThat(result.ignored()).isTrue();
    }

    private static ProcessProviderSubmissionRetryService service(FakeRepository repository,
            ProviderSubmissionClientPort providerClient) {
        return new ProcessProviderSubmissionRetryService(new ProviderSubmissionFailedMapper(), repository, providerClient,
                () -> NOW);
    }

    private static DomainEventEnvelope event(String type) {
        return new DomainEventEnvelope(UUID.randomUUID(), type, 1, NOW, COMPANY_ID, "Sale", SALE_ID, "billing-service",
                null, "retry-key", Map.of(
                        "saleId", SALE_ID.toString(),
                        "documentId", DOCUMENT_ID.toString(),
                        "documentType", "ELECTRONIC_POS",
                        "documentIdempotencyKey", "sale-confirm-001"));
    }

    private static BillingDocumentSnapshot snapshot(ElectronicDocumentStatus status, ProviderStatus providerStatus) {
        return new BillingDocumentSnapshot(COMPANY_ID, SALE_ID, null, "POS", "CONFIRMED", DOCUMENT_ID,
                "ELECTRONIC_POS", status, providerStatus, "POS", 1L, "CUFE-OLD", "QR-OLD",
                new BigDecimal("1000.00"), new BigDecimal("190.00"), new BigDecimal("1190.00"),
                "sale-confirm-001", NOW, List.of(new SaleLineSnapshot(UUID.randomUUID(), UUID.randomUUID(), "SKU-1",
                        "Cafe", "PHYSICAL_GOOD", true, BigDecimal.ONE, new BigDecimal("500.00"),
                        new BigDecimal("1000.00"), BigDecimal.ZERO, "IVA19", new BigDecimal("19.0000"),
                        new BigDecimal("1000.00"), new BigDecimal("190.00"), new BigDecimal("1190.00"))));
    }

    private static class FakeRepository implements BillingDocumentRetryRepositoryPort {
        private final BillingDocumentSnapshot snapshot;
        private final List<DomainEventEnvelope> publishedEvents = new ArrayList<>();
        private boolean accepted;
        private boolean rejected;
        private boolean failed;

        FakeRepository(BillingDocumentSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Optional<BillingDocumentSnapshot> findDocument(UUID companyId, UUID documentId) {
            return Optional.of(snapshot);
        }

        @Override
        public void markAcceptedAndPublish(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome,
                DomainEventEnvelope saleConfirmed, DomainEventEnvelope documentValidated, Instant retriedAt) {
            accepted = true;
            publishedEvents.add(saleConfirmed);
            publishedEvents.add(documentValidated);
        }

        @Override
        public void markRejected(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt) {
            rejected = true;
        }

        @Override
        public void markFailed(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt) {
            failed = true;
        }
    }
}