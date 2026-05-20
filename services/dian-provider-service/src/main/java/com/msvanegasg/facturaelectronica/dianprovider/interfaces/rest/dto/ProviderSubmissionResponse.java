package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProviderSubmissionResponse(
        UUID id,
        UUID companyId,
        UUID documentId,
        String documentType,
        String trackingId,
        String status,
        String cufeCude,
        String qrContent,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        List<ProviderArtifactResponse> artifacts,
        String rawResponse) {
}
