package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ElectronicDocumentTraceEvent(
        UUID id,
        UUID companyId,
        UUID documentId,
        ElectronicDocumentStatus previousStatus,
        ElectronicDocumentStatus newStatus,
        ElectronicDocumentTraceAction action,
        ProviderSubmissionStatus result,
        String detail,
        UUID userId,
        Instant occurredAt) {
}
