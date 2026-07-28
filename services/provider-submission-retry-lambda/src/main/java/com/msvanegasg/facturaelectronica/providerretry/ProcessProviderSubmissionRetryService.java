package com.msvanegasg.facturaelectronica.providerretry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class ProcessProviderSubmissionRetryService {

    private final ProviderSubmissionFailedMapper mapper;
    private final BillingDocumentRetryRepositoryPort repository;
    private final ProviderSubmissionClientPort providerClient;
    private final ClockPort clock;

    public ProcessProviderSubmissionRetryService(ProviderSubmissionFailedMapper mapper,
            BillingDocumentRetryRepositoryPort repository, ProviderSubmissionClientPort providerClient,
            ClockPort clock) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.providerClient = Objects.requireNonNull(providerClient, "providerClient is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public ProviderSubmissionRetryResult process(DomainEventEnvelope envelope) {
        if (!mapper.supports(envelope)) {
            return ProviderSubmissionRetryResult.ignoredResult();
        }
        ProviderSubmissionRetryRequest request = mapper.toRequest(envelope);
        BillingDocumentSnapshot snapshot = repository.findDocument(request.companyId(), request.documentId())
                .orElseThrow(() -> new IllegalArgumentException("billing document was not found: " + request.documentId()));
        if (snapshot.documentStatus() == ElectronicDocumentStatus.VALIDATED) {
            return ProviderSubmissionRetryResult.duplicateResult();
        }
        if (snapshot.documentStatus() == ElectronicDocumentStatus.REJECTED
                || snapshot.providerStatus() == ProviderStatus.REJECTED) {
            return ProviderSubmissionRetryResult.ignoredResult();
        }
        ProviderSubmissionOutcome outcome;
        try {
            outcome = providerClient.submit(snapshot);
        } catch (RuntimeException exception) {
            ProviderSubmissionOutcome failed = new ProviderSubmissionOutcome(ProviderStatus.FAILED,
                    snapshot.documentId().toString(), null, null, "PROVIDER_UNAVAILABLE", safeMessage(exception));
            repository.markFailed(snapshot, failed, clock.now());
            throw exception;
        }
        Instant retriedAt = clock.now();
        if (outcome.status() == ProviderStatus.ACCEPTED) {
            repository.markAcceptedAndPublish(snapshot, outcome, saleConfirmedEvent(snapshot, retriedAt),
                    documentValidatedEvent(snapshot, outcome, retriedAt), retriedAt);
            return ProviderSubmissionRetryResult.processedAccepted();
        }
        if (outcome.status() == ProviderStatus.REJECTED) {
            repository.markRejected(snapshot, outcome, retriedAt);
            return ProviderSubmissionRetryResult.processedRejected();
        }
        repository.markFailed(snapshot, outcome, retriedAt);
        throw new ProviderRetryStillFailedException("provider submission is still failed for document "
                + snapshot.documentId());
    }

    private DomainEventEnvelope saleConfirmedEvent(BillingDocumentSnapshot snapshot, Instant occurredAt) {
        return event(EventTypes.SALE_CONFIRMED, snapshot, salePayload(snapshot), occurredAt,
                snapshot.idempotencyKey() + ":provider-retry-sale-confirmed");
    }

    private DomainEventEnvelope documentValidatedEvent(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome,
            Instant occurredAt) {
        Map<String, Object> payload = documentPayload(snapshot, outcome);
        return event(EventTypes.ELECTRONIC_DOCUMENT_VALIDATED, snapshot, payload, occurredAt,
                snapshot.idempotencyKey() + ":provider-retry-document-validated");
    }

    private DomainEventEnvelope event(String eventType, BillingDocumentSnapshot snapshot, Map<String, Object> payload,
            Instant occurredAt, String idempotencyKey) {
        return new DomainEventEnvelope(UUID.randomUUID(), eventType, 1, occurredAt, snapshot.companyId(), "Sale",
                snapshot.saleId(), "provider-submission-retry-lambda", null, idempotencyKey, payload);
    }

    private static Map<String, Object> salePayload(BillingDocumentSnapshot snapshot) {
        Map<String, Object> payload = documentPayload(snapshot, null);
        payload.put("saleChannel", snapshot.saleChannel());
        payload.put("saleStatus", snapshot.saleStatus());
        payload.put("customerId", snapshot.customerId() == null ? null : snapshot.customerId().toString());
        payload.put("subtotal", snapshot.subtotal());
        payload.put("taxTotal", snapshot.taxTotal());
        payload.put("lines", snapshot.lines().stream().map(ProcessProviderSubmissionRetryService::linePayload).toList());
        payload.put("inventoryApplied", false);
        payload.put("accountingApplied", false);
        return payload;
    }

    private static Map<String, Object> documentPayload(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome) {
        String providerStatus = outcome == null ? snapshot.providerStatus().name() : outcome.status().name();
        String cufeCude = firstNonBlank(outcome == null ? null : outcome.cufeCude(), snapshot.cufeCude());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("saleId", snapshot.saleId().toString());
        payload.put("documentId", snapshot.documentId().toString());
        payload.put("documentType", snapshot.documentType());
        payload.put("documentStatus", "VALIDATED");
        payload.put("providerStatus", providerStatus);
        payload.put("prefix", snapshot.prefix());
        payload.put("documentNumber", snapshot.documentNumber());
        payload.put("documentIdempotencyKey", snapshot.idempotencyKey());
        payload.put("cufeCude", cufeCude);
        payload.put("total", snapshot.total());
        payload.put("issuedAt", snapshot.issuedAt().toString());
        return payload;
    }

    private static Map<String, Object> linePayload(SaleLineSnapshot line) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("lineId", line.lineId().toString());
        payload.put("productId", line.productId().toString());
        payload.put("productSku", line.productSku());
        payload.put("productName", line.productName());
        payload.put("itemType", line.itemType());
        payload.put("stockTracked", line.stockTracked());
        payload.put("quantity", line.quantity());
        payload.put("unitCost", valueOrZero(line.unitCost()));
        payload.put("unitPrice", line.unitPrice());
        payload.put("subtotal", line.subtotal());
        payload.put("taxAmount", line.taxAmount());
        payload.put("total", line.total());
        return payload;
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName()
                : message.replace('\n', ' ').replace('\r', ' ');
    }
}