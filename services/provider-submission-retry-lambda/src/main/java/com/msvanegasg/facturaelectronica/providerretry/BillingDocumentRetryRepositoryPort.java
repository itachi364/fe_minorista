package com.msvanegasg.facturaelectronica.providerretry;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public interface BillingDocumentRetryRepositoryPort {

    Optional<BillingDocumentSnapshot> findDocument(UUID companyId, UUID documentId);

    void markAcceptedAndPublish(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome,
            DomainEventEnvelope saleConfirmed, DomainEventEnvelope documentValidated, Instant retriedAt);

    void markRejected(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt);

    void markFailed(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt);
}