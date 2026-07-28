package com.msvanegasg.facturaelectronica.providerretry;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class ProviderSubmissionFailedMapper {

    public boolean supports(DomainEventEnvelope envelope) {
        return envelope != null && (EventTypes.PROVIDER_SUBMISSION_FAILED.equals(envelope.eventType())
                || EventTypes.PROVIDER_SUBMISSION_PENDING.equals(envelope.eventType()));
    }

    public ProviderSubmissionRetryRequest toRequest(DomainEventEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope is required");
        Map<String, Object> payload = envelope.payload();
        return new ProviderSubmissionRetryRequest(envelope.companyId(), uuid(payload, "saleId"),
                uuid(payload, "documentId"), text(payload, "documentType"), text(payload, "documentIdempotencyKey"));
    }

    private static UUID uuid(Map<String, Object> payload, String key) {
        return UUID.fromString(text(payload, key));
    }

    private static String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.toString().trim();
    }
}