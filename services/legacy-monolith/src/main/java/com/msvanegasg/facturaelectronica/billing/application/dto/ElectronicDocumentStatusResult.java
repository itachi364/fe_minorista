package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;

public record ElectronicDocumentStatusResult(
        UUID documentId,
        ElectronicDocumentStatus status,
        String providerSubmissionId,
        String cufeCude,
        String qrContent,
        String xmlContent,
        String graphicRepresentationContent,
        String errorCode,
        String errorMessage,
        Instant updatedAt) {
}
