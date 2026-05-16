package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_provider_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingProviderSubmissionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private ElectronicDocumentType documentType;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "request_payload_hash", nullable = false, length = 128)
    private String requestPayloadHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProviderSubmissionStatus status;

    @Column(name = "provider_submission_id", length = 120)
    private String providerSubmissionId;

    @Column(name = "cufe_cude", length = 200)
    private String cufeCude;

    @Column(name = "qr_content")
    private String qrContent;

    @Column(name = "xml_content")
    private String xmlContent;

    @Column(name = "graphic_representation_content")
    private String graphicRepresentationContent;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
}
