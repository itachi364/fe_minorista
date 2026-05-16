package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ElectronicDocumentLifecycle {

    private final UUID id;
    private final UUID companyId;
    private final ElectronicDocumentType documentType;
    private ElectronicDocumentStatus status;
    private String providerSubmissionId;
    private String cufeCude;
    private String qrContent;
    private String xmlContent;
    private String graphicRepresentationContent;
    private String errorCode;
    private String errorMessage;
    private Instant updatedAt;

    private ElectronicDocumentLifecycle(
            UUID id,
            UUID companyId,
            ElectronicDocumentType documentType,
            ElectronicDocumentStatus status,
            String providerSubmissionId,
            String cufeCude,
            String qrContent,
            String xmlContent,
            String graphicRepresentationContent,
            String errorCode,
            String errorMessage,
            Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.documentType = documentType;
        this.status = status;
        this.providerSubmissionId = providerSubmissionId;
        this.cufeCude = cufeCude;
        this.qrContent = qrContent;
        this.xmlContent = xmlContent;
        this.graphicRepresentationContent = graphicRepresentationContent;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updatedAt = updatedAt;
    }

    public static ElectronicDocumentLifecycle restore(
            UUID id,
            UUID companyId,
            ElectronicDocumentType documentType,
            ElectronicDocumentStatus status,
            String providerSubmissionId,
            String cufeCude,
            String qrContent,
            String xmlContent,
            String graphicRepresentationContent,
            String errorCode,
            String errorMessage,
            Instant updatedAt) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(documentType, "documentType");
        requireNonNull(status, "status");
        requireNonNull(updatedAt, "updatedAt");

        return new ElectronicDocumentLifecycle(
                id,
                companyId,
                documentType,
                status,
                blankToNull(providerSubmissionId),
                blankToNull(cufeCude),
                blankToNull(qrContent),
                blankToNull(xmlContent),
                blankToNull(graphicRepresentationContent),
                blankToNull(errorCode),
                blankToNull(errorMessage),
                updatedAt);
    }

    public ElectronicDocumentStatus previousStatusFor(ProviderSubmissionStatus providerStatus) {
        requireNonNull(providerStatus, "providerStatus");
        if (providerStatus == ProviderSubmissionStatus.ACCEPTED
                || providerStatus == ProviderSubmissionStatus.REJECTED
                || providerStatus == ProviderSubmissionStatus.FAILED) {
            requireStatus(ElectronicDocumentStatus.SENT_TO_PROVIDER);
        }
        return status;
    }

    public void applyProviderOutcome(
            ProviderSubmissionStatus providerStatus,
            String providerSubmissionId,
            String cufeCude,
            String qrContent,
            String xmlContent,
            String graphicRepresentationContent,
            String errorCode,
            String errorMessage,
            Instant occurredAt) {
        requireNonNull(providerStatus, "providerStatus");
        requireNonNull(occurredAt, "occurredAt");
        requireStatus(ElectronicDocumentStatus.SENT_TO_PROVIDER);

        this.providerSubmissionId = blankToNull(providerSubmissionId);
        this.updatedAt = occurredAt;

        if (providerStatus == ProviderSubmissionStatus.ACCEPTED) {
            requireNonBlank(cufeCude, "cufeCude");
            this.cufeCude = cufeCude.trim();
            this.qrContent = blankToNull(qrContent);
            this.xmlContent = blankToNull(xmlContent);
            this.graphicRepresentationContent = blankToNull(graphicRepresentationContent);
            this.errorCode = null;
            this.errorMessage = null;
            this.status = ElectronicDocumentStatus.VALIDATED;
            return;
        }

        if (providerStatus == ProviderSubmissionStatus.REJECTED) {
            this.errorCode = blankToNull(errorCode);
            this.errorMessage = blankToNull(errorMessage);
            this.status = ElectronicDocumentStatus.REJECTED;
            return;
        }

        this.errorCode = blankToNull(errorCode);
        this.errorMessage = blankToNull(errorMessage);
        this.status = ElectronicDocumentStatus.FAILED;
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public ElectronicDocumentType documentType() {
        return documentType;
    }

    public ElectronicDocumentStatus status() {
        return status;
    }

    public String providerSubmissionId() {
        return providerSubmissionId;
    }

    public String cufeCude() {
        return cufeCude;
    }

    public String qrContent() {
        return qrContent;
    }

    public String xmlContent() {
        return xmlContent;
    }

    public String graphicRepresentationContent() {
        return graphicRepresentationContent;
    }

    public String errorCode() {
        return errorCode;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void requireStatus(ElectronicDocumentStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException("document status must be " + expectedStatus);
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
